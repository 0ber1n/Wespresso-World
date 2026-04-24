package io.wespresso_world.wespresso_world.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Checkout: convert cart to an order", description = "Creates an order from the specified cart and clears the cart")
    @PostMapping("/checkout/{cartId}")
    public Order checkout(
            @PathVariable Long cartId,
            @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        return orderService.placeOrder(cartId, request.getShippingAddress(), userId, username);
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
}

class CheckoutRequest {
    @Schema(description = "Full shipping address for the order")
    private String shippingAddress;

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
