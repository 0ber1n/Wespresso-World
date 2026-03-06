package io.wespresso_world.wespresso_world.cart;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    private Long id;
    private String category;
    private Double price;
    private Integer quantity;   
    private String itemName;

    // Link all items to single cart
    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore // Prevents infinite recursion during JSON serialization
    private Cart cart;  
    
}
