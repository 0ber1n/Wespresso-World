package io.wespresso_world.wespresso_world.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data                  // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor     // Generates the emputy cotnructor JPA needs
@AllArgsConstructor    // Generates a constructor with all fields as parameters
@Table(name = "coffees") // Specifies the table name in the database
public class Coffee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    private Long id;
    private String name;
    private String description; 
    private Double price;
}
