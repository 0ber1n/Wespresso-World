package io.wespresso_world.wespresso_world.cart;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    private Long id;

    private String customerName;

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
