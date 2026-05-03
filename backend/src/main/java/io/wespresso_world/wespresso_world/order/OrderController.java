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
import io.wespresso_world.wespresso_world.VulnConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    // [SSTI] Inject VulnConfig to gate the vulnerable endpoint
    @Autowired
    private VulnConfig vulnConfig;

    @Operation(summary = "Checkout: convert cart to an order", description = "Creates an order from the specified cart and clears the cart")
    @PostMapping("/checkout/{cartId}")
    public Order checkout(
            @PathVariable Long cartId,
            @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
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
    public List<Order> getMyOrders(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
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

        //  VULNERABLE — orderNote from the database is concatenated into the template string
        // If the student saved a Thymeleaf expression as their order note at checkout,
        // it gets evaluated here when the receipt is rendered
        StringTemplateResolver stringResolver = new StringTemplateResolver();
        stringResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine stringEngine = new TemplateEngine();
        stringEngine.setTemplateResolver(stringResolver);

        Context context = new Context();
        context.setVariable("order", order);

        String note = order.getOrderNote() == null ? "" : order.getOrderNote();

        String templateString = "<html xmlns:th='http://www.thymeleaf.org'>"
            + "<body>"
            + "<div th:fragment='msg'>" + note + "</div>"
            + "</body></html>";

        String html = stringEngine.process(templateString, context);

        // Detect Level 1 — Process object in output means RCE was achieved
        if (html.contains("Process[")) {
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html + "\n<!-- flag{ssti_l1_rce_proven} -->");
        }

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}

class CheckoutRequest {
    @Schema(description = "Full shipping address for the order")
    private String shippingAddress;

    @Schema(description = "Optional gift message or order note")
    private String orderNote;
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getOrderNote() { return orderNote; }
    public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
}
