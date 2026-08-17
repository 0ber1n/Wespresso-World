package io.wespresso_world.wespresso_world;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.wespresso_world.wespresso_world.VulnConfig.PasswordIdor;


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
    private fileUploadMagicByteOnly fileUploadMagicByteOnly = new fileUploadMagicByteOnly();
    private FileUploadCdrBypass fileUploadCdrBypass = new FileUploadCdrBypass();
    private PasswordIdor passwordIdor = new PasswordIdor();
    private SstiThymeleaf sstiThymeleaf = new SstiThymeleaf();
    private SessionFixation sessionFixation = new SessionFixation();
    private Xxe xxe = new Xxe();
    private MassAssignment massAssignment = new MassAssignment();

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

    public static class fileUploadMagicByteOnly {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class FileUploadCdrBypass {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class PasswordIdor {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class SstiThymeleaf {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class SessionFixation {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Xxe {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class MassAssignment {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }


    public StoredXss getStoredXss() { return storedXss; }
    public FileUploadExtOnly getFileUploadExtOnly() { return fileUploadExtOnly; }
    public FileUploadExtEndsWith getFileUploadExtEndsWith() { return fileUploadExtEndsWith; }
    public fileUploadMagicByteOnly getFileUploadMagicByteOnly() { return fileUploadMagicByteOnly; }
    public FileUploadCdrBypass getFileUploadCdrBypass() { return fileUploadCdrBypass; }
    public JwtNone getJwtNone() { return jwtNone; }
    public SqliLogin getSqliLogin() { return sqliLogin; }
    public RateLimitBypass getRateLimitBypass() { return rateLimitBypass; }
    public CartIdor getCartIdor() { return cartIdor; }
    public PasswordIdor getPasswordIdor() { return passwordIdor; }
    public SstiThymeleaf getSstiThymeleaf() { return sstiThymeleaf; }
    public SessionFixation getSessionFixation() { return sessionFixation; }
    public Xxe getXxe() { return xxe; }
    public MassAssignment getMassAssignment() { return massAssignment; }


}