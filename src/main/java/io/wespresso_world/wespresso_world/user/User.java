package io.wespresso_world.wespresso_world.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Schema(description = "User account details")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Column(nullable = false)
    @Schema(description = "Password of the user", example = "password123")
    private String password;

    @Column(nullable = false, unique = true)
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Role of the user", example = "USER")
    private Role role;

    public enum Role {
        USER,
        ADMIN
    }
}
