package io.wespresso_world.wespresso_world.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.VulnConfig;

import java.util.Base64;
import java.util.Map;

// [NEW] Admin controller — holds the JWT none CTF flag endpoint
@Tag(name = "Admin API", description = "Endpoints for admin operations")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private JwtService jwtService;

    // [NEW] Inject VulnConfig to gate the flag on the jwt_none vuln toggle
    @Autowired
    private VulnConfig vulnConfig;

    // [NEW] Flag endpoint — only reachable by admin role
    // Visible in Swagger and Network tab (403 breadcrumb for normal users)
    // Returns flag only when accessed with a forged alg:none JWT
    // AND the jwt_none vuln is enabled in config
    @Operation(summary = "Admin flag", description = "Restricted admin endpoint")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/flag")
    public Map<String, String> getFlag(@RequestHeader("Authorization") String authHeader) {

        // [NEW] If jwt_none vuln is disabled, this endpoint exists but never gives the flag
        if (!vulnConfig.getJwtNone().isEnabled()) {
            return Map.of("message", "Nothing to see here.");
        }

        String token = authHeader.substring(7);
        String[] parts = token.split("\\.");

        // [NEW] Check if the token was forged with alg:none
        boolean usedNoneAlg = false;
        try {
            if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("\"none\"")) {
                    usedNoneAlg = true;
                }
            }
        } catch (Exception ignored) {}

        // [NEW] Only return the flag if a forged none token was used
        if (usedNoneAlg) {
            return Map.of(
                "flag", "wes{n0n3_$hall_p@55}",
                "message", "You forged a JWT with alg:none and escalated to admin."
            );
        }

        // Legitimate admin hitting this endpoint gets nothing interesting
        return Map.of("message", "Nothing to see here.");
    }
}