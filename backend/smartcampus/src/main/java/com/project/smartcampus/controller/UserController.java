package com.project.smartcampus.controller;

import com.project.smartcampus.enums.Role;
import com.project.smartcampus.services.AuthService;
import com.project.smartcampus.util.HttpUtil;
import com.project.smartcampus.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    public boolean canHandle(String path) {
        return "/users/me".equals(path)
                || "/users/roles".equals(path)
                || "/api/users/me".equals(path)
                || "/api/users/roles".equals(path);
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        if ("/users/me".equals(path)) {
            handleCurrentUser(exchange);
            return;
        }
        if ("/users/roles".equals(path)) {
            handleRoleUpdate(exchange);
            return;
        }
        HttpUtil.sendJson(exchange, 404, JsonUtil.message("Route not found."));
    }

    private void handleCurrentUser(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            HttpUtil.sendJson(exchange, 200, JsonUtil.userResponse(user));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleRoleUpdate(HttpExchange exchange) throws IOException {
        if (!"PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        String body = HttpUtil.readBody(exchange);
        String email = JsonUtil.extractString(body, "email");
        String roleValue = JsonUtil.extractString(body, "role");
        if (email == null || roleValue == null) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.message("Email and role are required."));
            return;
        }

        try {
            Role role = Role.valueOf(roleValue.trim().toUpperCase());
            AuthService.AuthenticatedUser updated =
                    authService.updateRole(exchange.getRequestHeaders().getFirst("Authorization"), email, role);
            HttpUtil.sendJson(exchange, 200, JsonUtil.userResponse(updated));
        } catch (IllegalArgumentException exception) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.message(exception.getMessage()));
        } catch (IllegalStateException exception) {
            HttpUtil.sendJson(exchange, 403, JsonUtil.message("Forbidden"));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private String normalizePath(String path) {
        if (path.startsWith("/api/users")) {
            return path.substring("/api".length());
        }
        return path;
    }
}
