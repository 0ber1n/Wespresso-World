package io.wespresso_world.wespresso_world.giftcard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.FlagConfig;
import io.wespresso_world.wespresso_world.VulnConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Tag(name = "Gift Card API", description = "Redeem digital gift cards")
@RestController
@RequestMapping("/gift-card")
public class GiftCardController {

    @Autowired
    private VulnConfig vulnConfig;

    @Autowired
    private FlagConfig flagConfig;

    private static final Map<String, Double> VALID_CODES = Map.of(
        "WESPRESSO-143", 20.00,
        "WESPRESSO-369", 20.00,
        "WESPRESSO-400", 20.00
    );

    // VULNERABLE when VULN_XXE_ENABLED=true — DocumentBuilderFactory resolves external entities by default
    // Secure path sets FEATURE_SECURE_PROCESSING and disables DOCTYPE declarations entirely
    // Attack: define <!ENTITY xxe SYSTEM "file:///var/secrets/master_key"> and reference &xxe; in <code>
    // The invalid-code error path echoes the parsed code back, so the XXE payload is reflected in the 400 response
    @Operation(summary = "Redeem a Wespresso gift card")
    @PostMapping(value = "/redeem", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> redeemGiftCard(@RequestBody String xmlBody) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            if (!vulnConfig.getXxe().isEnabled()) {
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlBody.getBytes(StandardCharsets.UTF_8)));

            String code = elementText(doc, "code").trim();

            if (isPasswd(code)) {
                return ResponseEntity.ok(Map.of(
                    "message", flagConfig.getXxe()
                ));
            }

            Double amount = VALID_CODES.get(code.toUpperCase());
            if (amount == null) {
                return ResponseEntity.badRequest().body("Gift card '" + code + "' not recognised.");
            }

            return ResponseEntity.ok(Map.of(
                "message", "Gift card applied to your account",
                "code",    code.toUpperCase(),
                "amount",  String.format("%.2f", amount)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid gift card: " + e.getMessage());
        }
    }

    private String elementText(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }

    // Detects /etc/passwd content by the presence of root's passwd entry structure
    private boolean isPasswd(String content) {
        return content != null && content.contains("root:") && content.contains(":/bin/");
    }
}
