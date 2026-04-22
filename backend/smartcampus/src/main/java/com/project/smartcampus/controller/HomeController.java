package com.project.smartcampus.controller;

import com.project.smartcampus.enums.Role;
import com.project.smartcampus.services.AuthService;
import com.project.smartcampus.services.NotificationService;
import com.project.smartcampus.util.HttpUtil;
import com.project.smartcampus.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HomeController {
    private final AuthService authService;
    private final NotificationService notificationService;

    public HomeController(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        this.notificationService = notificationService;
    }

    public boolean canHandle(String path) {
        return "/actuator/health".equals(path) || "/admin/dashboard/summary".equals(path);
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/actuator/health".equals(path)) {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtil.sendMethodNotAllowed(exchange);
                return;
            }
            HttpUtil.sendJson(exchange, 200, JsonUtil.health());
            return;
        }

        if ("/admin/dashboard/summary".equals(path)) {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtil.sendMethodNotAllowed(exchange);
                return;
            }

            try {
                AuthService.AuthenticatedUser admin = authService.requireRole(exchange.getRequestHeaders().getFirst("Authorization"), Role.ADMIN);
                String payload = JsonUtil.adminSummary(
                        authService.totalUsers(),
                        notificationService.totalNotifications(admin.email()),
                        notificationService.unreadNotifications(admin.email()));
                HttpUtil.sendJson(exchange, 200, payload);
            } catch (IllegalStateException exception) {
                HttpUtil.sendJson(exchange, 403, JsonUtil.message("Forbidden"));
            } catch (SecurityException exception) {
                HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
            }
            return;
        }

        HttpUtil.sendJson(exchange, 404, JsonUtil.message("Route not found."));
    }
}
