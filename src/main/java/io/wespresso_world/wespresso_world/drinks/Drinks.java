package io.wespresso_world.wespresso_world.drinks;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor     // Generates the empty constructor JPA needs
@AllArgsConstructor    // Generates a constructor with all fields as parameters
@Table(name = "drinks") // Specifies the table name in the database
public class Drinks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    private Long id;
    private String name;
    private String description; 
    private Double price;
}
