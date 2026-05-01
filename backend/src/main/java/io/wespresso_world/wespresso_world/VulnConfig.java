package io.wespresso_world.wespresso_world;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "vuln")
public class VulnConfig {

    private JwtNone jwtNone = new JwtNone();
    private SqliLogin sqliLogin = new SqliLogin();
    private RateLimitBypass rateLimitBypass = new RateLimitBypass();
    private CartIdor cartIdor = new CartIdor();
    private StoredXss storedXss = new StoredXss();
    private FileUploadExtOnly fileUploadExtOnly = new FileUploadExtOnly();
    private FileUploadExtEndsWith fileUploadExtEndsWith = new FileUploadExtEndsWith();
    private FileUploadMimeOnly fileUploadMimeOnly = new FileUploadMimeOnly();
    private FileUploadCdrBypass fileUploadCdrBypass = new FileUploadCdrBypass();
    
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

    public static class CartIdor {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class StoredXss {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class FileUploadExtOnly {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class FileUploadExtEndsWith {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class FileUploadMimeOnly {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class FileUploadCdrBypass {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public StoredXss getStoredXss() { return storedXss; }
    public FileUploadExtOnly getFileUploadExtOnly() { return fileUploadExtOnly; }
    public FileUploadExtEndsWith getFileUploadExtEndsWith() { return fileUploadExtEndsWith; }
    public FileUploadMimeOnly getFileUploadMimeOnly() { return fileUploadMimeOnly; }
    public FileUploadCdrBypass getFileUploadCdrBypass() { return fileUploadCdrBypass; }
    public JwtNone getJwtNone() { return jwtNone; }
    public SqliLogin getSqliLogin() { return sqliLogin; }
    public RateLimitBypass getRateLimitBypass() { return rateLimitBypass; }
    public CartIdor getCartIdor() { return cartIdor; }
    

    
}