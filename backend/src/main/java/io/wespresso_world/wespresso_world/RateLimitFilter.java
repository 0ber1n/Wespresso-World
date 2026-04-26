package io.wespresso_world.wespresso_world;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private VulnConfig vulnConfig;

    private static final int MAX_FAILURES = 4;
    private static final long LOCKOUT_MS = 30_000;

    private static class AttemptRecord {
        int failures = 0;
        long lockoutUntil = 0;
    }

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/auth/login") && !path.equals("/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (vulnConfig.getRateLimitBypass().isEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        
        String ip = resolveClientIp(request);
        long now = System.currentTimeMillis();

        AttemptRecord record = attempts.computeIfAbsent(ip, k -> new AttemptRecord());

        synchronized (record) {
            if (now < record.lockoutUntil) {
                long secondsLeft = (record.lockoutUntil - now + 999) / 1000;
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                    "{\"error\":\"Too many failed attempts. Try again in " + secondsLeft + " second(s).\"}"
                );
                return;
            }
            // Lockout expired — reset so the IP gets a fresh window
            if (record.lockoutUntil > 0) {
                record.failures = 0;
                record.lockoutUntil = 0;
            }
        }

        StatusCapturingResponse wrapper = new StatusCapturingResponse(response);
        chain.doFilter(request, wrapper);
        int status = wrapper.getCapturedStatus();

        synchronized (record) {
            if (status >= 200 && status < 300) {
                attempts.remove(ip);
            } else {
                record.failures++;
                if (record.failures >= MAX_FAILURES) {
                    record.lockoutUntil = System.currentTimeMillis() + LOCKOUT_MS;
                    record.failures = 0;
                }
            }
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class StatusCapturingResponse extends HttpServletResponseWrapper {
        private int status = 200;

        StatusCapturingResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int status) {
            super.setStatus(status);
            this.status = status;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.status = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.status = sc;
        }

        int getCapturedStatus() {
            return status;
        }
    }
}
