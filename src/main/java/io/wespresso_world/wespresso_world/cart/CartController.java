package io.wespresso_world.wespresso_world.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Creats a new cart for a customer (POST /cart with body {"customerName": "John"})
    @PostMapping
    public Cart createCart(@RequestBody CartRequest request) {
        return cartService.createCart(request.getCustomerName());
    }

    // Adds an item to the cart  (POST /cart/{cartId}/add-drink with body {"drinkId": 1, "quantity": 2})
    @PostMapping("/{cartId}/add-drink")
    public Cart addDrinkToCart(@PathVariable Long cartId, @RequestBody CartRequest request) {
        return cartService.addDrinkToCart(cartId, request.getDrinkId(), request.getQuantity());
    }

    
}

class CartRequest {
    private Long drinkId;
    private Integer quantity;
    private String customerName;

    // Getters and Setters (or use @Data if you move to its own file)
    public Long getDrinkId() { return drinkId; }
    public Integer getQuantity() { return quantity; }
    public String getCustomerName() { return customerName; }

    public void setDrinkId(Long drinkId) { this.drinkId = drinkId;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}
    public void setCustomerName(String customerName) { this.customerName = customerName;}
}