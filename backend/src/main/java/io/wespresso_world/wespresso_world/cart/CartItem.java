package io.wespresso_world.wespresso_world.cart;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
@Schema (description = "Details of an item in the shopping cart") // Adds OpenAPI schema description for API documentation
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Auto-generates the ID
    
    @Schema(description = "Unique identifier for the cart item")
    private Long id;

    @Schema(description = "Category of the drink")
    private String category;

    @Schema(description = "Price of the drink")
    private Double price;

    @Schema(description = "Quantity of the drink")
    private Integer quantity;

    @Schema(description = "Name of the drink")
    private String itemName;

    // Link all items to single cart
    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore // Prevents infinite recursion during JSON serialization
    @Schema(hidden = true) // Hides the cart field from API documentation since it's an internal link
    private Cart cart;  
    
}
