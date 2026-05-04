package io.wespresso_world.wespresso_world.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringReader;
import java.io.StringWriter;
import io.wespresso_world.wespresso_world.VulnConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.user.JwtService;
import io.wespresso_world.wespresso_world.user.SecurityHelper;
import io.wespresso_world.wespresso_world.user.SecurityHelper;
import io.wespresso_world.wespresso_world.user.JwtService;

import java.util.List;

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

            // Detect Level 1 — any expression was evaluated
            // FreeMarker replaces ${...} so if output differs from input, execution happened
            if (!html.contains(note) || html.contains("Process[")) {
                String flagBanner = "<div style='background:#d4edda;border:2px solid #28a745;padding:16px;"
                    + "margin:20px;border-radius:8px;font-family:monospace;font-size:14px;'>"
                    + "<strong>SSTI Level 1 Flag:</strong> flag{ssti_l1_rce_proven}\n"
                    + "<strong>What do you think flag.txt says?"
                    + "</div>";
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(flagBanner + html + "\n<!-- flag{ssti_l1_rce_proven} -->");
            }
            if (html.contains("flag{")) {
            String flagBanner = "<div style='background:#d4edda;border:2px solid #28a745;padding:16px;"
                + "margin:20px;border-radius:8px;font-family:monospace;font-size:14px;'>"
                + "<strong>Level 2 Flag:</strong> " + html.replaceAll(".*flag\\{([^}]+)\\}.*", "flag{$1}")
                + "</div>";
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(flagBanner + html);
        }
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Template error: " + e.getMessage());
        }
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