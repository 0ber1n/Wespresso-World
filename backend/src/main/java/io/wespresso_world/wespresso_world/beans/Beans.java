package io.wespresso_world.wespresso_world.beans;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor     // Generates the empty constructor JPA needs
@AllArgsConstructor    // Generates a constructor with all fields as parameters
@Table(name = "beans") // Specifies the table name in the database  
@Schema(description = "Coffee bean product details") // Adds OpenAPI schema description for API documentation
public class Beans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    
    @Schema(description = "Unique identifier for the coffee bean")
    private Long id;

    @Schema(description = "Name of the coffee bean")
    private String name;
    
    @Schema(description = "Description of the coffee bean")
    private String description;
    
    @Schema(description = "Origin of the coffee bean")
    private String origin;
    
    @Schema(description = "Roast level of the coffee bean")
    private String roastLevel;
    
    @Schema(description = "Indicates if the coffee bean is raw")    
    private Boolean isRaw;
    
    @Schema(description = "Price of the coffee bean")
    private Double price;
}

