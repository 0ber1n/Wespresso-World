package io.wespresso_world.wespresso_world.giftcard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.FlagConfig;
import io.wespresso_world.wespresso_world.VulnConfig;
import io.wespresso_world.wespresso_world.user.User;
import io.wespresso_world.wespresso_world.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "Gift Card API", description = "Redeem digital gift cards")
@RestController
@RequestMapping("/gift-card")
public class GiftCardController {

    @Autowired
    private VulnConfig vulnConfig;

    @Autowired
    private FlagConfig flagConfig;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Double> VALID_CODES = Map.of(
        "WESPRESSO-143", 20.00,
        "WESPRESSO-369", 20.00,
        "WESPRESSO-400", 20.00
    );

    // Content-type gating: JSON always accepted; XML only accepted when VULN_XXE_ENABLED=true.
    // When XXE is enabled, the XML path uses a bare DocumentBuilderFactory that resolves external
    // entities — attackers intercept the JSON request in Burp, switch Content-Type to application/xml,
    // and inject a DOCTYPE entity pointing at file:///etc/passwd. The invalid-code error echoes the
    // resolved entity content back, leaking the file (or flagConfig.getXxe() on /etc/passwd detection).
    @Operation(summary = "Redeem a Wespresso gift card")
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemGiftCard(
            @RequestBody String body,
            @RequestHeader(value = "Content-Type", defaultValue = "application/json") String contentType) {

        boolean isXml = contentType.toLowerCase().contains("application/xml");

        if (isXml && !vulnConfig.getXxe().isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("XML is not accepted. Use application/json.");
        }

        User user = currentUser();

        try {
            String code;
            Double overrideAmount = null;

            if (isXml) {
                code = parseCodeFromXml(body);
            } else {
                JsonNode node = objectMapper.readTree(body);
                JsonNode codeNode = node.get("code");
                if (codeNode == null) throw new IllegalArgumentException("Missing 'code' field");
                code = codeNode.asText().trim();

                // Mass assignment: server blindly trusts client-supplied amount
                if (vulnConfig.getMassAssignment().isEnabled()) {
                    JsonNode amountNode = node.get("amount");
                    if (amountNode != null && !amountNode.isNull()) {
                        overrideAmount = amountNode.asDouble();
                    }
                }
            }

            if (isPasswd(code)) {
                return ResponseEntity.ok(Map.of("message", flagConfig.getXxe()));
            }

            String normalised = code.toUpperCase();

            if (user.getRedeemedGiftCodes().contains(normalised)) {
                return ResponseEntity.badRequest()
                        .body("Gift card '" + normalised + "' has already been redeemed on this account.");
            }

            Double catalogAmount = VALID_CODES.get(normalised);
            if (catalogAmount == null) {
                return ResponseEntity.badRequest().body("Gift card '" + code + "' not recognised.");
            }

            double appliedAmount = overrideAmount != null ? overrideAmount : catalogAmount;

            user.getRedeemedGiftCodes().add(normalised);
            user.setCreditBalance(user.getCreditBalance() + appliedAmount);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Gift card applied to your account",
                    "code", normalised,
                    "amount", String.format("%.2f", appliedAmount)));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid gift card: " + e.getMessage());
        }
    }

    @Operation(summary = "Get gift card credit balance for the current user")
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        User user = currentUser();
        return ResponseEntity.ok(Map.of(
                "balance", user.getCreditBalance(),
                "redeemedCodes", List.copyOf(user.getRedeemedGiftCodes())));
    }

    // Vulnerable when VULN_XXE_ENABLED=true — no DOCTYPE restriction, external entities resolve freely
    private String parseCodeFromXml(String body) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        return elementText(doc, "code").trim();
    }

    private String elementText(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }

    private boolean isPasswd(String content) {
        return content != null && content.contains("root:") && content.contains(":/bin/");
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
