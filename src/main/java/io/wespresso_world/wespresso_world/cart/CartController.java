package io.wespresso_world.wespresso_world.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cart API", description = "Endpoints for managing shopping carts") // Adds OpenAPI tag for grouping endpoints in documentation
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Creats a new cart for a customer (POST /cart with body {"customerName": "John"})
    @Operation(summary = "Create a new shopping cart", description = "Creates a new shopping cart for a customer") // Adds OpenAPI operation summary and description for API documentation
    @PostMapping
    public Cart createCart(@RequestBody CartRequest request) {
        return cartService.createCart(request.getCustomerName());
    }

    // Adds an item to the cart  (POST /cart/{cartId}/add-drink with body {"drinkId": 1, "quantity": 2})
    @Operation(summary = "Add a drink to the shopping cart", description = "Adds a drink to the specified shopping cart") // Adds OpenAPI operation summary and description for API documentation
    @PostMapping("/{cartId}/add-drink")
    public Cart addDrinkToCart(@PathVariable Long cartId, @RequestBody CartRequest request) {
        return cartService.addDrinkToCart(cartId, request.getDrinkId(), request.getQuantity());
    }

    
}

class CartRequest {
    @Schema(description = "Unique identifier for the drink to be added to the cart")
    private Long drinkId;

    @Schema(description = "Quantity of the drink to be added to the cart")
    private Integer quantity;
    
    @Schema(description = "Name of the customer who owns the shopping cart")
    private String customerName;

    // Getters and Setters (or use @Data if you move to its own file)
    public Long getDrinkId() { return drinkId; }
    public Integer getQuantity() { return quantity; }
    public String getCustomerName() { return customerName; }

    public void setDrinkId(Long drinkId) { this.drinkId = drinkId;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}
    public void setCustomerName(String customerName) { this.customerName = customerName;}
}