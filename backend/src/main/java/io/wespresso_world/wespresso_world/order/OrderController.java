package io.wespresso_world.wespresso_world.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import io.wespresso_world.wespresso_world.FlagConfig;
import io.wespresso_world.wespresso_world.VulnConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.user.JwtService;
import io.wespresso_world.wespresso_world.user.SecurityHelper;

import java.util.List;
import java.util.Map;

@Tag(name = "Order API", description = "Endpoints for placing and viewing orders")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private VulnConfig vulnConfig;

    @Autowired
    private FlagConfig flagConfig;

    @Autowired
    private Configuration freeMarkerConfiguration;

    @Operation(summary = "Checkout: convert cart to an order", description = "Creates an order from the specified cart and clears the cart")
    @PostMapping("/checkout/{cartId}")
    public Order checkout(
            @PathVariable Long cartId,
            @RequestBody CheckoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long   userId   = SecurityHelper.getUserId(authHeader, jwtService);
        String username = SecurityHelper.getUsername(authHeader, jwtService);
        return orderService.placeOrder(cartId, request.getShippingAddress(), request.getOrderNote(), userId, username);
    }

    @Operation(summary = "Get an order by ID")
    @PreAuthorize("hasRole('admin') or authentication.name == @orderService.getOrderOwnerUsername(#orderId)")
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @Operation(summary = "Get all orders for the authenticated user")
    @GetMapping("/my-orders")
    public List<Order> getMyOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = SecurityHelper.getUserId(authHeader, jwtService);
        return orderService.getOrdersByUser(userId);
    }

    // SAFE receipt — reads orderNote from saved order, renders it as plain text
    @Operation(summary = "View order receipt (safe)")
    @GetMapping("/{orderId}/receipt")
    public ResponseEntity<String> getReceipt(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        Order order = orderService.getOrder(orderId);

        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        // orderNote is data — th:text in the template encodes it, never evaluates it
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("orderNote", order.getOrderNote());

        String html = templateEngine.process("receipt", context);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }


    // VULNERABLE receipt — orderNote is concatenated into template string
    // Only active when VULN_SSTI_THYMELEAF_ENABLED=true
    @Operation(summary = "View order receipt beta (vulnerable to SSTI)")
    @GetMapping("/{orderId}/receipt-beta")
    public ResponseEntity<String> getReceiptBeta(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        Order order = orderService.getOrder(orderId);

        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        // Safe fallback when vuln is disabled
        if (!vulnConfig.getSstiThymeleaf().isEnabled()) {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("orderNote", order.getOrderNote());
            String html = templateEngine.process("receipt", context);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        }

        // VULNERABLE — orderNote concatenated into FreeMarker template string
        // FreeMarker evaluates ${...} expressions — user input becomes executable
        try {
            String note = order.getOrderNote() == null ? "" : order.getOrderNote();

            // User input is embedded directly into the template string
            // If note contains ${7*7} FreeMarker evaluates it to 49
            // If note contains ${"freemarker.template.utility.Execute"?new()("cat /flag.txt")}
            // FreeMarker executes the command and returns the output
            String templateString = "<html><body>"
                + "<div class='note'>" + note + "</div>"
                + "</body></html>";

            Template template = new Template("receipt-beta",
                new StringReader(templateString),
                freeMarkerConfiguration);

            StringWriter writer = new StringWriter();
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("order", order);
            template.process(model, writer);

            String html = writer.toString();

            // Level 2 — RCE: player read /flag.txt via command execution
            if (html.contains("flag{")) {
                String flagBanner = "<div style='background:#d4edda;border:2px solid #28a745;padding:16px;"
                    + "margin:20px;border-radius:8px;font-family:monospace;font-size:14px;'>"
                    + "<strong>SSTI Level 2 Flag:</strong> " + html.replaceAll(".*flag\\{([^}]+)\\}.*", "flag{$1}")
                    + "</div>";
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(flagBanner + html);
            }

            // Level 1 — SSTI triggered: any expression was evaluated
            // FreeMarker replaces ${...} so if output differs from input, execution happened
            if (!html.contains(note) || html.contains("Process[")) {
                String flagBanner = "<div style='background:#d4edda;border:2px solid #28a745;padding:16px;"
                    + "margin:20px;border-radius:8px;font-family:monospace;font-size:14px;'>"
                    + "<strong>SSTI Level 1 Flag:</strong> flag{ssti_l1_rce_proven}<br>"
                    + "<strong>Hint:</strong> What do you think /flag.txt says?"
                    + "</div>";
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(flagBanner + html + "\n<!-- flag{ssti_l1_rce_proven} -->");
            }
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Template error: " + e.getMessage());
        }
}

    // Harder XXE — looks like a plain JSON order export; XML accepted when VULN_XXE_HARD_ENABLED=true.
    // Attacker intercepts in Burp, switches Content-Type to application/xml, adds a DOCTYPE external
    // entity, and references it inside a <field> element. An unknown field name is echoed in the
    // error response, leaking the resolved entity content (e.g. /etc/passwd).
    @Operation(summary = "Export order history as JSON")
    @PostMapping("/export")
    public ResponseEntity<?> exportOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String body,
            @RequestHeader(value = "Content-Type", defaultValue = "application/json") String contentType) {

        boolean isXml = contentType.toLowerCase().contains("application/xml");

        if (isXml && !vulnConfig.getXxeHard().isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("XML is not accepted. Use application/json.");
        }

        Long userId = SecurityHelper.getUserId(authHeader, jwtService);
        List<Order> orders = orderService.getOrdersByUser(userId);

        List<String> fields;
        try {
            fields = isXml ? parseFieldsFromXml(body) : parseFieldsFromJson(body);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String field : fields) {
                switch (field.toLowerCase().trim()) {
                    case "id"      -> row.put("id", order.getId());
                    case "date"    -> row.put("date", order.getCreatedAt());
                    case "total"   -> row.put("total", order.getTotalPrice());
                    case "status"  -> row.put("status", order.getStatus());
                    case "address" -> row.put("address", order.getShippingAddress());
                    case "items"   -> row.put("items", order.getItems().stream()
                        .map(i -> Map.of("name", i.getItemName(), "qty", i.getQuantity(), "price", i.getPrice()))
                        .toList());
                    default -> {
                        if (exportIsPasswd(field)) {
                            return ResponseEntity.ok(Map.of("message", flagConfig.getXxeHard()));
                        }
                        return ResponseEntity.badRequest().body("Unknown field: '" + field + "'");
                    }
                }
            }
            result.add(row);
        }
        return ResponseEntity.ok(result);
    }

    private List<String> parseFieldsFromXml(String body) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        NodeList nodes = doc.getElementsByTagName("fields");
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            fields.add(nodes.item(i).getTextContent());
        }
        return fields;
    }

    private List<String> parseFieldsFromJson(String body) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(body);
        JsonNode fieldsNode = node.get("fields");
        if (fieldsNode == null || !fieldsNode.isArray()) {
            throw new IllegalArgumentException("'fields' array is required");
        }
        List<String> fields = new ArrayList<>();
        for (JsonNode f : fieldsNode) {
            fields.add(f.asText());
        }
        return fields;
    }

    private boolean exportIsPasswd(String content) {
        return content != null && content.contains("root:") && content.contains(":/bin/");
    }

    static class CheckoutRequest {
    @Schema(description = "Full shipping address for the order")
    private String shippingAddress;

    @Schema(description = "Optional gift message or order note")
    private String orderNote;
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getOrderNote() { return orderNote; }
    public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
    }

}