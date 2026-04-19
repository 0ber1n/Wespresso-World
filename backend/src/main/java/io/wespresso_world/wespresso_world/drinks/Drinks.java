package io.wespresso_world.wespresso_world.drinks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor     // Generates the empty constructor JPA needs
@AllArgsConstructor    // Generates a constructor with all fields as parameters
@Table(name = "drinks") // Specifies the table name in the database
@Schema(description = "Drink product details") // Adds OpenAPI schema description for API documentation 
public class Drinks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    @Schema(description = "Unique identifier for the drink")
    private Long id;

    @Schema(description = "Name of the drink")
    private String name;

    @Schema(description = "Description of the drink")
    private String description;
    
    @Schema(description = "Price of the drink")
    private Double price;
}
