package io.wespresso_world.wespresso_world;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/vuln-flags")
public class VulnFlagsController {

    @Autowired
    private VulnConfig vulnConfig;

    @GetMapping
    public Map<String, Boolean> getFlags() {
        return Map.of("storedXss", vulnConfig.getStoredXss().isEnabled());
    }
}
