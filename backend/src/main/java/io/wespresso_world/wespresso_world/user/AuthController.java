package io.wespresso_world.wespresso_world.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.FlagConfig;
import io.wespresso_world.wespresso_world.VulnConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    // [NEW] Raw JDBC for vulnerable SQLi login path
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // [NEW] Vulnerability toggles
    @Autowired
    private VulnConfig vulnConfig;

    @Autowired
    private FlagConfig flagConfig;

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
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");

        // [NEW] Vulnerable SQLi login path — enabled by VULN_SQLI_LOGIN_ENABLED=true
        // Uses raw string concatenation instead of parameterized queries
        // Vulnerable to: ' OR '1'='1'-- login bypass
        // Flag is stored in steve's email field, extractable via UNION injection
        if (vulnConfig.getSqliLogin().isEnabled()) {
            try {
                // [VULN] Direct string concatenation — never do this in production
                String sql = "SELECT id, username, password, email, role FROM users " +
                             "WHERE username = '" + username + "'";

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
                    // If username matches exactly, require password check
                    // If injection occurred (username doesn't match returned user), allow through
                    if (user.getUsername().equals(username) &&
                        !passwordEncoder.matches(password, user.getPassword())) {
                        return ResponseEntity.badRequest().body("Invalid username or password");
                    }
                    String token = jwtService.generateToken(user);
                    if (vulnConfig.getSessionFixation().isEnabled()) {
                        HttpSession session = httpRequest.getSession(true);
                        session.setAttribute("username", user.getUsername());
                        session.setAttribute("role", user.getRole().name());
                        session.setAttribute("userId", user.getId());
                    }
                    return ResponseEntity.ok(Map.of("token", token));
                } else {
                    return ResponseEntity.badRequest().body("Invalid username or password");
                }

            } catch (Exception e) {
                System.out.println("SQLi query error: " + e.getMessage());
                // [CHANGED] Return 500 on SQL error so sqlmap can distinguish
                // true injection (200) vs false injection (400) vs error (500)
                return ResponseEntity.status(500).body("Internal error");
            }
        }

        // Normal safe login path — parameterized via Spring Data JPA
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        // [VULN] Session fixation — session is created before auth and never rotated.
        // A student watching the Network tab will see the same JSESSIONID
        // before and after login. Pre-setting it before submitting credentials
        // means the attacker controls the session ID the victim lands in.
        if (vulnConfig.getSessionFixation().isEnabled()) {
            HttpSession session = httpRequest.getSession(true); // reuse pre-existing or create new
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole().name());
            session.setAttribute("userId", user.getId());
        }
        return ResponseEntity.ok(Map.of("token", token));
    }

   @Operation(summary = "Get the profile of the authenticated user")
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = SecurityHelper.getUsername(authHeader, jwtService);
        Long   userID   = SecurityHelper.getUserId(authHeader, jwtService);
        String email    = SecurityHelper.getEmail(authHeader, jwtService);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("user");

        return ResponseEntity.ok(Map.of(
            "id",       userID,
            "username", username,
            "email",    email,
            "role",     role
        ));
    }

    // [VULN] Session fixation — logout does not invalidate the server-side session.
    // The client loses its JWT, but the JSESSIONID remains valid on the server.
    // A student should notice the cookie persists and still grants access post-logout.
    @Operation(summary = "Log out the current user")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        if (vulnConfig.getSessionFixation().isEnabled()) {
            // Intentionally does nothing server-side — session lives on
            return ResponseEntity.ok("Logged out");
        }

        // Secure path: actually destroy the session
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("Logged out");
    }

    @Operation(summary = "Update the authenticated user's email")
    @PatchMapping("/profile/email")
    public ResponseEntity<?> updateEmail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        String newEmail = request.get("email");
        if (newEmail == null || newEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (userRepository.findByEmail(newEmail).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        Long userId = SecurityHelper.getUserId(authHeader, jwtService);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(newEmail);
        userRepository.save(user);
        return ResponseEntity.ok("Email updated successfully");
    }

    @Operation(summary = "Upload an avatar for the authenticated user")
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file) throws Exception {

        Long userId = SecurityHelper.getUserId(authHeader, jwtService);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // SCENARIO 1 — Weak contains() extension check
        // Defense:  filename must contain .jpg or .jpeg anywhere in the name
        // Missing:  endsWith() enforcement, Content-Type check, magic byte check, re-encoding
        // Bypass:   double extension — upload shell.jpg.exe, shell.jpg.py, etc.
        //           .jpg appears in the name so the check passes, but the true
        //           extension is whatever comes last. The OS and execution context
        //           use the final extension to decide how to handle the file.
        if (vulnConfig.getFileUploadExtOnly().isEnabled()) {
            String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!originalName.contains(".jpg") && !originalName.contains(".jpeg")) {
                return ResponseEntity.badRequest().body("Only JPG files are allowed");
            }
            byte[] bytes = file.getBytes();
            user.setAvatar(bytes);
            user.setAvatarContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            userRepository.save(user);
            // Flag if the final extension is not .jpg/.jpeg — double extension bypass succeeded
            String finalExt = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
            if (!finalExt.equals(".jpg") && !finalExt.equals(".jpeg")) {
                return ResponseEntity.ok("Avatar uploaded successfully\n" + flagConfig.getFileUploadExtOnly());
            }
            return ResponseEntity.ok("Avatar uploaded successfully");
        }

        // SCENARIO 2 — Correct endsWith() extension check + Content-Type check, no magic byte validation
        // Defense:  filename must end in .jpg or .jpeg, Content-Type must be image/jpeg
        // Missing:  magic byte check, re-encoding
        // Bypass:   send a real .jpg filename and Content-Type: image/jpeg but with non-JPEG
        //           bytes inside. Both client-controlled checks pass but the server never
        //           inspects the actual file content. Any bytes get stored.
        if (vulnConfig.getFileUploadExtEndsWith().isEnabled()) {
            String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!originalName.endsWith(".jpg") && !originalName.endsWith(".jpeg")) {
                return ResponseEntity.badRequest().body("Only JPG files are allowed");
            }
            // Content-Type check still active — only the missing magic byte check is the weak point
            String contentType = file.getContentType() == null ? "" : file.getContentType();
            if (!contentType.equals("image/jpeg")) {
                return ResponseEntity.badRequest().body("Only JPG files are allowed");
            }
            byte[] bytes = file.getBytes();
            user.setAvatar(bytes);
            user.setAvatarContentType("image/jpeg");
            userRepository.save(user);
            // Flag if bytes don't start with JPEG magic bytes FF D8 FF — non-JPEG content got through
            if (bytes.length < 3 || (bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF) {
                return ResponseEntity.ok("Avatar uploaded successfully\n" + flagConfig.getFileUploadEndswith());
            }
            return ResponseEntity.ok("Avatar uploaded successfully");
        }

        // SCENARIO 3 — endsWith() extension check + Content-Type header check + magic byte check
        // Defense:  filename must end in .jpg/.jpeg, Content-Type must be image/jpeg,
        //           and first three bytes must be FF D8 FF (JPEG magic bytes)
        // Missing:  full structural JPEG validation via ImageIO
        // Bypass:   craft a file that starts with FF D8 FF but is not a valid JPEG structure.
        //           A polyglot file with JPEG magic bytes prepended passes all three checks
        //           but ImageIO rejects the malformed structure.
        if (vulnConfig.getFileUploadMagicByteOnly().isEnabled()) {
            String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!originalName.endsWith(".jpg") && !originalName.endsWith(".jpeg")) {
                return ResponseEntity.badRequest().body("Only JPG files are allowed");
            }
            String contentType = file.getContentType() == null ? "" : file.getContentType();
            if (!contentType.equals("image/jpeg")) {
                return ResponseEntity.badRequest().body("Only JPG files are allowed");
            }
            byte[] bytes = file.getBytes();
            // Magic byte check still active — only trusting Content-Type without ImageIO is the weak point
            if (bytes.length < 3 || (bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF) {
                return ResponseEntity.badRequest().body("Only JPG files are allowed");
            }
            user.setAvatar(bytes);
            user.setAvatarContentType("image/jpeg");
            userRepository.save(user);
            // Check for appended payload after EOI marker — polyglot technique
            int eoiIndex = -1;
            for (int i = 0; i < bytes.length - 1; i++) {
                if ((bytes[i] & 0xFF) == 0xFF && (bytes[i + 1] & 0xFF) == 0xD9) {
                    eoiIndex = i;
                }
            }
            if (eoiIndex != -1 && eoiIndex + 2 < bytes.length) {
                return ResponseEntity.ok("Avatar uploaded successfully!\n\nFile upload vulnerability? Yes. This specific attack? Not quite.");
            }

            // Flag if ImageIO rejects it — magic bytes passed but it's not a valid JPEG structure
            BufferedImage check = null;
            try {
                check = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                // malformed image — treat same as null
            }
            if (check == null) {
                return ResponseEntity.ok("Avatar uploaded successfully\n" + flagConfig.getFileUploadMagicByte());
            }
            return ResponseEntity.ok("Avatar uploaded successfully");
        }

        // SCENARIO 4 — endsWith() + Content-Type + ImageIO validation, no CDR
        // Defense:  filename, Content-Type, AND ImageIO.read() must all pass
        // Missing:  Content Disarm and Reconstruct (CDR) — the canvas re-encode step
        // Bypass:   craft a polyglot JPEG — a valid image that also carries a payload
        //           in EXIF metadata, JPEG comments, or bytes appended after the EOI marker.
        //           ImageIO.read() accepts it as a real image, but without re-encoding
        //           to a blank canvas the raw payload bytes survive into storage.
        //           Tools: exiftool -comment='<payload>' real.jpg
        if (vulnConfig.getFileUploadCdrBypass().isEnabled()) {
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
            // No canvas re-encode — raw bytes stored, embedded payload survives.
            byte[] rawBytes = file.getBytes();
            user.setAvatar(rawBytes);
            user.setAvatarContentType("image/jpeg");
            userRepository.save(user);
            // Flag if there are bytes after the JPEG EOI marker FF D9 — polyglot payload survived
            boolean payloadFound = false;
            int eoiIndex = -1;
            for (int i = 0; i < rawBytes.length - 1; i++) {
                if ((rawBytes[i] & 0xFF) == 0xFF && (rawBytes[i + 1] & 0xFF) == 0xD9) {
                    eoiIndex = i;
                }
            }
            System.out.println("Last EOI at index: " + eoiIndex + " file length: " + rawBytes.length);
            if (eoiIndex != -1 && eoiIndex + 2 < rawBytes.length) {
                payloadFound = true;
            }
            if (payloadFound) {
                return ResponseEntity.ok("Avatar uploaded successfully\n" + flagConfig.getFileUploadCdr());
            }
            return ResponseEntity.ok("Avatar uploaded successfully");
        }

    
        // SECURE — All four layers active (default)
        // 1. endsWith() extension check
        // 2. Content-Type header check
        // 3. ImageIO.read() — file must parse as a real image
        // 4. CDR — redraw to blank canvas, stripping all metadata and embedded payloads
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
        user.setAvatar(out.toByteArray());
        user.setAvatarContentType("image/jpeg");
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
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("New password is required");
        }

        Long userId;

        if (vulnConfig.getPasswordIdor().isEnabled()) {
            // VULN: trust the userId from the request body instead of the JWT
            String requestedId = request.get("userId");
            if (requestedId == null) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            userId = Long.parseLong(requestedId);
            // No current password check — attacker doesn't know victim's password
        } else {
            // SECURE: always derive userId from the JWT or session
            userId = SecurityHelper.getUserId(authHeader, jwtService);
            if (currentPassword == null || currentPassword.isBlank()) {
                return ResponseEntity.badRequest().body("Current and new password are required");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(403).body("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return ResponseEntity.ok("Password updated successfully");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated successfully");
    }
}