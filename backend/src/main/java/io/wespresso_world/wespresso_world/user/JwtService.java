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
        if (vulnConfig.getJwtNone().isEnabled()) {
            String[] parts = token.split("\\.");
            if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                String role = extractClaimFromNoneToken(token, "role");
                if (role != null) return role;
            }
        }
        return extractClaims(token).get("role", String.class);
    }

    public String extractEmail(String token) {
        if (vulnConfig.getJwtNone().isEnabled()) {
            String[] parts = token.split("\\.");
            if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                String email = extractClaimFromNoneToken(token, "email");
                if (email != null) return email;
            }
        }
        return extractClaims(token).get("email", String.class);
    }


    public String extractUsername(String token) {
        if (vulnConfig.getJwtNone().isEnabled()) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                if (header.toLowerCase().contains("none")) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    // extract sub from payload
                    int subIndex = payload.indexOf("\"sub\":\"");
                    if (subIndex != -1) {
                        int start = subIndex + 7;
                        int end = payload.indexOf("\"", start);
                        return payload.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            // fall through to normal extraction
        }
        }
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        if (vulnConfig.getJwtNone().isEnabled()) {
            String[] parts = token.split("\\.");
            if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                String userId = extractClaimFromNoneToken(token, "userId");
                if (userId != null) return Long.parseLong(userId);
            }
        }
        return extractClaims(token).get("userId", Long.class);
    }

    // Checks the token validity. This includes a vulnerable switch for none.
    public boolean isTokenValid(String token, String username) {
        if (vulnConfig.getJwtNone().isEnabled()){
            try {
                String[] parts = token.split("\\.");
                if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())){
                    String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                    if (header.toLowerCase().contains("\"none\"")){
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        return payload.contains("\"sub\":\"" + username + "\"");
                    }
                }
            } catch (Exception e){
                // Fall through to normal validation
            }
        }
        String extractedUsername = extractUsername(token);
        return extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token);
    }

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
    String role = extractRole(token);
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
}

    private boolean isTokenExpired(String token) {
        if (vulnConfig.getJwtNone().isEnabled()) {
            String[] parts = token.split("\\.");
            if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                return false; // none tokens never expire in vuln mode
            }
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

