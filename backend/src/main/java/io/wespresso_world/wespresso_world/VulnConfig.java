package io.wespresso_world.wespresso_world;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vuln")
public class VulnConfig {

    private JwtNone jwtNone = new JwtNone();
    private SqliLogin sqliLogin = new SqliLogin();
    private RateLimitBypass rateLimitBypass = new RateLimitBypass();

    public static class JwtNone {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class SqliLogin {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class RateLimitBypass {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public JwtNone getJwtNone() { return jwtNone; }
    public SqliLogin getSqliLogin() { return sqliLogin; }
    public RateLimitBypass getRateLimitBypass() { return rateLimitBypass; }
}