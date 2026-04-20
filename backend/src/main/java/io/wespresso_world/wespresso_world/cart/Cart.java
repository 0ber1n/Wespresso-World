package io.wespresso_world.wespresso_world.cart;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
@Schema(description = "Shopping cart details") // Adds OpenAPI schema description for API documentation 
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Auto-generates the ID
    
    @Schema(description = "Unique identifier for the shopping cart")
    private Long id;

    @Schema(description = "Name of the customer who owns the shopping cart")        
    private String customerName;

    @Schema(description = "Unique identifier for the user associated with the shopping cart")
    private Long userId;

    // One cart can have multiple items
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Double getTotalPrice() {
        return items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this); // Set the cart reference in the item
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null); // Remove the cart reference from the item
    }
}
