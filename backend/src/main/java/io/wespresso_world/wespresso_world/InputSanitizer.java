package io.wespresso_world.wespresso_world;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class InputSanitizer {

    public String sanitize(String input, String fieldName) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.contains("<") || trimmed.contains(">")) {
            throw new IllegalArgumentException("Invalid characters in: " + fieldName);
        }
        String lower = trimmed.toLowerCase();
        if (lower.contains("script") || lower.contains("onerror")
                || lower.contains("onload") || lower.contains("javascript:")) {
            throw new IllegalArgumentException("Invalid characters in: " + fieldName);
        }
        if (trimmed.contains(";") || trimmed.contains("|") || trimmed.contains("&")
                || trimmed.contains("`") || trimmed.contains("$(") || trimmed.contains("\0")) {
            throw new IllegalArgumentException("Invalid characters in: " + fieldName);
        }
        return trimmed;
    }

    public Map<String, String> sanitizeAll(Map<String, String> fields) {
        fields.replaceAll((key, value) -> sanitize(value, key));
        return fields;
    }
}
