package io.wespresso_world.wespresso_world.user;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class SecurityHelper {

    // Pulls userId from SecurityContext details (session path)
    // or falls back to extracting it from the JWT
    public static Long getUserId(String authHeader, JwtService jwtService) {
        Long id = getDetailValue("userId", Long.class);
        if (id != null) return id;
        if (authHeader != null) return jwtService.extractUserId(authHeader.substring(7));
        return null;
    }

    // Pulls username from SecurityContext (works for both session and JWT paths)
    public static String getUsername(String authHeader, JwtService jwtService) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) return auth.getName();
        if (authHeader != null) return jwtService.extractUsername(authHeader.substring(7));
        return null;
    }

    // Pulls email from SecurityContext details (session path)
    // or falls back to JWT
    public static String getEmail(String authHeader, JwtService jwtService) {
        String email = getDetailValue("email", String.class);
        if (email != null && !email.isEmpty()) return email;
        if (authHeader != null) return jwtService.extractEmail(authHeader.substring(7));
        return null;
    }

    // Reads a typed value out of the auth token's details map
    @SuppressWarnings("unchecked")
    private static <T> T getDetailValue(String key, Class<T> type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object details = token.getDetails();
            if (details instanceof Map<?, ?> map) {
                Object value = map.get(key);
                if (type.isInstance(value)) return type.cast(value);
            }
        }
        return null;
    }
}