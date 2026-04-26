package io.wespresso_world.wespresso_world.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.VulnConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;


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

    @Autowired
    private JdbcTemplate jdbcTemplate; // For SQLi login vuln testing

    @Autowired
    private VulnConfig vulnConfig; // For vulnerability toggles

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
        newUser.setRole(User.Role.user);
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(summary = "Authenticate a user and generate JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (vulnConfig.getSqliLogin().isEnabled()) {
        try {
            // [VULN] Direct string concatenation — never do this in production
            String sql = "SELECT id, username, password, email, role FROM users " +
                         "WHERE username = '" + username + "' " +
                         "AND password = '" + password + "'";

            List<User> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                u.setRole(User.Role.valueOf(rs.getString("role")));
                return u;
            });

            if (!results.isEmpty()) {
                User user = results.get(0);
                String token = jwtService.generateToken(user);
                return ResponseEntity.ok(Map.of("token", token));
            }
            return ResponseEntity.badRequest().body("Invalid username or password");

        } catch (Exception e) {
            // [NOTE] Swallow SQL errors to avoid leaking schema info
            // Students can infer injection worked from response changes
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Operation(summary = "Get the profile of the authenticated user")
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        Long userID = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);
        String email = jwtService.extractEmail(token);

        return ResponseEntity.ok(Map.of(
            "id", userID,
            "username", username,
            "email", email,
            "role", role
        ));
    }

    @Operation(summary = "Update the authenticated user's email")
    @PatchMapping("/profile/email")
    public ResponseEntity<?> updateEmail(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        String newEmail = request.get("email");
        if (newEmail == null || newEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (userRepository.findByEmail(newEmail).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(newEmail);
        userRepository.save(user);
        return ResponseEntity.ok("Email updated successfully");
    }

    @Operation(summary = "Upload a JPG avatar for the authenticated user")
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!originalName.endsWith(".jpg") && !originalName.endsWith(".jpeg")) {
            return ResponseEntity.badRequest().body("Only JPG files are allowed");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!contentType.equals("image/jpeg")) {
            return ResponseEntity.badRequest().body("Only JPG files are allowed");
        }

        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            return ResponseEntity.badRequest().body("Could not read image");
        }

        BufferedImage canvas = new BufferedImage(96, 96, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, 96, 96, null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(canvas, "jpg", out);

        Long userId = jwtService.extractUserId(authHeader.substring(7));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatar(out.toByteArray());
        userRepository.save(user);

        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    @Operation(summary = "Get a user's avatar by user ID")
    @GetMapping("/profile/avatar/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getAvatar() != null)
                .map(u -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(u.getAvatar()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update the authenticated user's password")
    @PatchMapping("/profile/password")
    public ResponseEntity<?> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        if (currentPassword == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Current and new password are required");
        }
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(403).body("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated successfully");
    }
}
