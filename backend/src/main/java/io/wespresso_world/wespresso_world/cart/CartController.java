package io.wespresso_world.wespresso_world.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cart API", description = "Endpoints for managing shopping carts") // Adds OpenAPI tag for grouping endpoints in documentation
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Get cart by ID
    @Operation(summary = "Get a cart by ID")
    @PreAuthorize("hasRole('admin') or authentication.name == @cartService.getCartOwner(#cartId)")
    @GetMapping("/{cartId}")
    public Cart getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }

    // Checks user owns the cart
    @Operation(summary = "Get the owner of the shopping cart", description = "Returns the name of the customer who owns the specified shopping cart") // Adds OpenAPI operation summary and description for API documentation
    @PreAuthorize("hasRole('admin') or authentication.name == @cartService.getCartOwner(#cartId)") // Ensures that only the owner of the cart can access it
    @GetMapping("/{cartId}/owner")
    public String getCartOwner(@PathVariable Long cartId) {
        return cartService.getCartOwner(cartId);
    }

    // Creats a new cart for a customer (POST /cart with body {"customerName": "John"})
    @Operation(summary = "Create a new shopping cart", description = "Creates a new shopping cart for a customer") // Adds OpenAPI operation summary and description for API documentation
    @PostMapping
    public Cart createCart(@RequestBody CartRequest request) {
        return cartService.createCart(request.getCustomerName());
    }

    // Adds an item to the cart  (POST /cart/{cartId}/add-drink with body {"drinkId": 1, "quantity": 2})
    @Operation(summary = "Add a drink to the shopping cart", description = "Adds a drink to the specified shopping cart") // Adds OpenAPI operation summary and description for API documentation
    @PreAuthorize("hasRole('admin') or authentication.name == @cartService.getCartOwner(#cartId)")
    @PostMapping("/{cartId}/add-drink")
    public Cart addDrinkToCart(@PathVariable Long cartId, @RequestBody CartRequest request) {
        return cartService.addDrinkToCart(cartId, request.getDrinkId(), request.getQuantity());
    }

    // Adds beans to the cart  (POST /cart/{cartId}/add-beans with body {"beansId": 1, "quantity": 2})
    @Operation(summary = "Add coffee beans to the shopping cart", description = "Adds coffee beans to the specified shopping cart") // Adds OpenAPI operation summary and description for API documentation
    @PreAuthorize("hasRole('admin') or authentication.name == @cartService.getCartOwner(#cartId)")
    @PostMapping("/{cartId}/add-beans")
    public Cart addBeansToCart(@PathVariable Long cartId, @RequestBody CartRequest request) {
        return cartService.addBeansToCart(cartId, request.getBeansId(), request.getQuantity());
    }   
    

}
class CartRequest {
    @Schema(description = "Unique identifier for the drink to be added to the cart")
    private Long drinkId;

    @Schema(description = "Unique identifier for the coffee beans to be added to the cart")
    private Long beansId;

    @Schema(description = "Quantity of the drink to be added to the cart")
    private Integer quantity;
    
    @Schema(description = "Name of the customer who owns the shopping cart")
    private String customerName;

    // Getters and Setters (or use @Data if you move to its own file)
    public Long getDrinkId() { return drinkId; }
    public Integer getQuantity() { return quantity; }
    public String getCustomerName() { return customerName; }
    public Long getBeansId() { return beansId; }

    public void setBeansId(Long beansId) { this.beansId = beansId;}
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}
    public void setCustomerName(String customerName) { this.customerName = customerName;}

   

}