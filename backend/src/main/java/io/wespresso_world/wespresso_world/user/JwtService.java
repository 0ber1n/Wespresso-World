package io.wespresso_world.wespresso_world.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.wespresso_world.wespresso_world.VulnConfig;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class JwtService {

    @Autowired
    private VulnConfig  vulnConfig;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("sub", user.getUsername()); // set subject via claims

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    private String extractClaimFromNoneToken(String token, String claimKey) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                // Try string value first ("key":"value")
                int keyIndex = payload.indexOf("\"" + claimKey + "\":\"");
                if (keyIndex != -1) {
                    int start = keyIndex + claimKey.length() + 4;
                    int end = payload.indexOf("\"", start);
                    return payload.substring(start, end);
                }
                // Try numeric value ("key":123)
                keyIndex = payload.indexOf("\"" + claimKey + "\":");
                if (keyIndex != -1) {
                    int start = keyIndex + claimKey.length() + 3;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.indexOf("}", start);
                    return payload.substring(start, end).trim();
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }
    
    public String extractRole(String token) {
        String[] parts = token.split("\\.");
        if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
            try {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("none")) {
                    if (vulnConfig.getJwtNone().isEnabled()) {
                        String role = extractClaimFromNoneToken(token, "role");
                        if (role != null) return role;
                    }
                    return null; // reject silently when vuln disabled
                }
            } catch (Exception e) { }
        }
        return extractClaims(token).get("role", String.class);
    }

    public String extractEmail(String token) {
        String[] parts = token.split("\\.");      
        if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
            try {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("none")) {
                    if (vulnConfig.getJwtNone().isEnabled()) {
                        String email = extractClaimFromNoneToken(token, "email");
                        if (email != null) return email;
                    }
                    return null; // reject silently when vuln disabled
                }
            } catch (Exception e) { }
        }
        return extractClaims(token).get("email", String.class);
    }


    public String extractUsername(String token) {
    // Always detect none tokens regardless of vuln flag
    // Without this, JJWT throws UnsupportedJwtException when vuln is disabled
    // and a none token is presented, crashing the filter chain
        String[] parts = token.split("\\.");
        if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
            try {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("none")) {
                    if (vulnConfig.getJwtNone().isEnabled()) {
                        // Vuln enabled — extract username from forged token
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        int subIndex = payload.indexOf("\"sub\":\"");
                        if (subIndex != -1) {
                            int start = subIndex + 7;
                            int end = payload.indexOf("\"", start);
                            return payload.substring(start, end);
                        }
                    }
                    // Vuln disabled — reject none token silently
                    return null;
                }
            } catch (Exception e) {
                // fall through
            }
        }
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        String[] parts = token.split("\\.");
        if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
            try {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("none")) {
                    if (vulnConfig.getJwtNone().isEnabled()) {
                        String userIdStr = extractClaimFromNoneToken(token, "userId");
                        if (userIdStr != null) return Long.parseLong(userIdStr);
                    }
                    return null; // reject silently when vuln disabled
                }
            } catch (Exception e) { }
        }
        return extractClaims(token).get("userId", Long.class);
    }

    // Checks the token validity. This includes a vulnerable switch for none.
    public boolean isTokenValid(String token, String username) {
        String tokenUsername = extractUsername(token);
        return (tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token));
    }
        

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        String role = extractRole(token);
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private boolean isTokenExpired(String token) {
        String[] parts = token.split("\\.");
        if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
            try {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("none")) {
                    if (vulnConfig.getJwtNone().isEnabled()) {
                        return false; // none tokens never expire in vuln mode
                    }
                    return true; // treat as expired when vuln disabled
                }
            } catch (Exception e) { }
        }
        return extractClaims(token).getExpiration().before(new Date());
    }
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

