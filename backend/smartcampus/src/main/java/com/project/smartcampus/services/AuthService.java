package com.project.smartcampus.services;

import com.project.smartcampus.config.JwtUtil;
import com.project.smartcampus.config.SeedProperties;
import com.project.smartcampus.dto.AuthResponse;
import com.project.smartcampus.enums.Role;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;

    public AuthService(SeedProperties seedProperties, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        putUser(seedProperties.userName(), seedProperties.userEmail(), seedProperties.userPassword(), Role.USER);
        putUser(seedProperties.adminName(), seedProperties.adminEmail(), seedProperties.adminPassword(), Role.ADMIN);
    }

    public synchronized AuthResponse signup(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (users.containsKey(normalizedEmail)) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        putUser(name, normalizedEmail, password, Role.USER);
        return login(normalizedEmail, password);
    }

    public AuthResponse login(String email, String password) {
        UserAccount user = users.get(normalizeEmail(email));
        if (user == null || !user.password().equals(password)) {
            throw new SecurityException("Invalid email or password.");
        }

        return new AuthResponse(jwtUtil.generateToken(user.email(), user.role()), user.email(), user.role());
    }

    public AuthenticatedUser requireAuthenticated(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        JwtUtil.TokenClaims claims = jwtUtil.parseToken(token);
        UserAccount user = users.get(normalizeEmail(claims.email()));
        if (user == null) {
            throw new SecurityException("User not found for token subject.");
        }
        return new AuthenticatedUser(user.name(), user.email(), user.role());
    }

    public AuthenticatedUser requireRole(String authorizationHeader, Role requiredRole) {
        AuthenticatedUser user = requireAuthenticated(authorizationHeader);
        if (user.role() != requiredRole) {
            throw new IllegalStateException("Forbidden");
        }
        return user;
    }

    public synchronized AuthenticatedUser updateRole(String authorizationHeader, String email, Role role) {
        requireRole(authorizationHeader, Role.ADMIN);

        String normalizedEmail = normalizeEmail(email);
        UserAccount existing = users.get(normalizedEmail);
        if (existing == null) {
            throw new IllegalArgumentException("User not found.");
        }

        UserAccount updated = new UserAccount(existing.name(), normalizedEmail, existing.password(), role);
        users.put(normalizedEmail, updated);
        return new AuthenticatedUser(updated.name(), updated.email(), updated.role());
    }

    public int totalUsers() {
        return users.size();
    }

    private void putUser(String name, String email, String password, Role role) {
        users.put(normalizeEmail(email), new UserAccount(name, normalizeEmail(email), password, role));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing bearer token.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record UserAccount(String name, String email, String password, Role role) {
    }

    public record AuthenticatedUser(String name, String email, Role role) {
    }
}
