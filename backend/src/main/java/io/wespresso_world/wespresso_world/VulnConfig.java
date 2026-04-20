package io.wespresso_world.wespresso_world;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vuln")
public class VulnConfig {

    private JwtNone jwtNone = new JwtNone();

    public static class JwtNone {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public JwtNone getJwtNone() { return jwtNone; }
}