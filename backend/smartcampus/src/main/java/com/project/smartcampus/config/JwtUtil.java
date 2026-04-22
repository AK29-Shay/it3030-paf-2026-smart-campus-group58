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
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtUtil(String secret, long expirationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String email, Role role) {
        return generateToken(null, email, role, null);
    }

    public String generateToken(Object user) {
        Long userId = asLong(invoke(user, "getId"));
        String email = asString(invoke(user, "getEmail"));
        Role role = asRole(invoke(user, "getRole"));
        String name = asString(invoke(user, "getName"));
        return generateToken(userId, email, role, name);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public TokenClaims parseToken(String token) {
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
        String subject = extract(payload, SUBJECT_PATTERN, "Missing subject.");
        String email = optional(payload, EMAIL_PATTERN);
        if (email == null && subject.contains("@")) {
            email = subject;
        }

        String roleValue = extract(payload, ROLE_PATTERN, "Missing role.");
        long expiresAt = Long.parseLong(extract(payload, EXP_PATTERN, "Missing expiry."));
        Long userId = asLong(optional(payload, USER_ID_PATTERN));
        if (userId == null && subject.chars().allMatch(Character::isDigit)) {
            userId = Long.parseLong(subject);
        }

        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("Token has expired.");
        }

        return new TokenClaims(email, Role.valueOf(roleValue), expiresAt, userId);
    }

    public Long extractUserId(String token) {
        return parseToken(token).userId();
    }

    public String extractEmail(String token) {
        return parseToken(token).email();
    }

    public String extractRole(String token) {
        Role role = parseToken(token).role();
        return role == null ? null : role.name();
    }

    private String generateToken(Long userId, String email, Role role, String name) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + expirationSeconds;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String subject = email != null && !email.isBlank()
                ? email
                : (userId == null ? "" : String.valueOf(userId));

        StringBuilder payload = new StringBuilder("{")
                .append("\"sub\":\"").append(escape(subject)).append("\"")
                .append(",\"role\":\"").append(role == null ? "" : role.name()).append("\"")
                .append(",\"iat\":").append(issuedAt)
                .append(",\"exp\":").append(expiresAt);
        if (email != null && !email.isBlank()) {
            payload.append(",\"email\":\"").append(escape(email)).append("\"");
        }
        if (userId != null) {
            payload.append(",\"userId\":").append(userId);
        }
        if (name != null && !name.isBlank()) {
            payload.append(",\"name\":\"").append(escape(name)).append("\"");
        }
        payload.append('}');

        String headerPart = encode(header);
        String payloadPart = encode(payload.toString());
        String signaturePart = sign(headerPart + "." + payloadPart);
        return headerPart + "." + payloadPart + "." + signaturePart;
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

    private String optional(String payload, Pattern pattern) {
        Matcher matcher = pattern.matcher(payload);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Object invoke(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Role asRole(Object value) {
        if (value instanceof Role role) {
            return role;
        }
        if (value == null) {
            return null;
        }
        try {
            return Role.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public record TokenClaims(String email, Role role, long expiresAt, Long userId) {
    }
}
