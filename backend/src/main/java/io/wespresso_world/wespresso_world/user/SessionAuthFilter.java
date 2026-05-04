package io.wespresso_world.wespresso_world.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.wespresso_world.wespresso_world.VulnConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private VulnConfig vulnConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (vulnConfig.getSessionFixation().isEnabled()) {

            HttpSession session = request.getSession(false);

            if (session != null) {
                String username = (String) session.getAttribute("username");
                String role     = (String) session.getAttribute("role");
                Long   userId   = (Long)   session.getAttribute("userId");
                String email    = (String) session.getAttribute("email");

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // Store userId and email in details so controllers can
                    // read full identity from SecurityContext without touching the JWT
                    auth.setDetails(Map.of(
                        "userId", userId,
                        "email",  email != null ? email : ""
                    ));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}