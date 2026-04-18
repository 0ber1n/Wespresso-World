package io.wespresso_world.wespresso_world.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "Auth API", description = "Endpoints for user authentication and registration")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setRole(User.Role.USER);
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(summary = "Authenticate a user and generate JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

}
