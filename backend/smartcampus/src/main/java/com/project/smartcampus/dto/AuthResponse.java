package com.project.smartcampus.dto;

import com.project.smartcampus.enums.Role;

public class AuthResponse {
    private final String accessToken;
    private final String email;
    private final Role role;
    private final String tokenType;
    private final Object user;

    public AuthResponse(String accessToken, String email, Role role) {
        this(accessToken, email, role, "Bearer", null);
    }

    private AuthResponse(String accessToken, String email, Role role, String tokenType, Object user) {
        this.accessToken = accessToken;
        this.email = email;
        this.role = role;
        this.tokenType = tokenType;
        this.user = user;
    }

    public static AuthResponse of(String token, Object user) {
        return new AuthResponse(token, null, null, "Bearer", user);
    }

    public String accessToken() {
        return accessToken;
    }

    public String email() {
        return email;
    }

    public Role role() {
        return role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getToken() {
        return accessToken;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Object getUser() {
        return user;
    }
}
