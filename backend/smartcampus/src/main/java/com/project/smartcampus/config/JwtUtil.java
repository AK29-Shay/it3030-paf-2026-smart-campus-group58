package com.project.smartcampus.config;

import com.project.smartcampus.enums.Role;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JwtUtil {
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtUtil(String secret, long expirationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String email, Role role) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + expirationSeconds;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + escape(email) + "\",\"role\":\"" + role.name() + "\",\"iat\":" + issuedAt + ",\"exp\":" + expiresAt + "}";

        String headerPart = encode(header);
        String payloadPart = encode(payload);
        String signaturePart = sign(headerPart + "." + payloadPart);
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    public TokenClaims validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing bearer token.");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token format.");
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!expectedSignature.equals(parts[2])) {
            throw new IllegalArgumentException("Invalid token signature.");
        }

        String payload = decode(parts[1]);
        String email = extract(payload, SUBJECT_PATTERN, "Missing subject.");
        String roleValue = extract(payload, ROLE_PATTERN, "Missing role.");
        long expiresAt = Long.parseLong(extract(payload, EXP_PATTERN, "Missing expiry."));
        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("Token has expired.");
        }

        return new TokenClaims(email, Role.valueOf(roleValue), expiresAt);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT.", exception);
        }
    }

    private String extract(String payload, Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(payload);
        if (!matcher.find()) {
            throw new IllegalArgumentException(message);
        }
        return matcher.group(1);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record TokenClaims(String email, Role role, long expiresAt) {
    }
}
