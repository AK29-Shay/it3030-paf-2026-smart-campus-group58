package com.project.smartcampus.controller;

import com.project.smartcampus.dto.AuthResponse;
import com.project.smartcampus.services.AuthService;
import com.project.smartcampus.services.NotificationService;
import com.project.smartcampus.util.HttpUtil;
import com.project.smartcampus.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class AuthController {
    private final AuthService authService;
    private final NotificationService notificationService;

    public AuthController(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        this.notificationService = notificationService;
    }

    public boolean canHandle(String path) {
        return "/auth/signup".equals(path) || "/auth/login".equals(path);
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/auth/signup".equals(path)) {
            handleSignup(exchange);
            return;
        }
        if ("/auth/login".equals(path)) {
            handleLogin(exchange);
            return;
        }
        HttpUtil.sendJson(exchange, 404, JsonUtil.message("Route not found."));
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        String body = HttpUtil.readBody(exchange);
        String name = JsonUtil.extractString(body, "name");
        String email = JsonUtil.extractString(body, "email");
        String password = JsonUtil.extractString(body, "password");
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.message("Name, email, and password are required."));
            return;
        }

        try {
            AuthResponse response = authService.signup(name, email, password);
            notificationService.addWelcomeNotification(response.email(), name);
            HttpUtil.sendJson(exchange, 201, JsonUtil.authResponse(response));
        } catch (IllegalArgumentException exception) {
            HttpUtil.sendJson(exchange, 409, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        String body = HttpUtil.readBody(exchange);
        String email = JsonUtil.extractString(body, "email");
        String password = JsonUtil.extractString(body, "password");
        if (isBlank(email) || isBlank(password)) {
            HttpUtil.sendJson(exchange, 400, JsonUtil.message("Email and password are required."));
            return;
        }

        try {
            AuthResponse response = authService.login(email, password);
            HttpUtil.sendJson(exchange, 200, JsonUtil.authResponse(response));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
