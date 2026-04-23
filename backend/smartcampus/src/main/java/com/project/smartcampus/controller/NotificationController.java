package com.project.smartcampus.controller;

import com.project.smartcampus.dto.NotificationDTO;
import com.project.smartcampus.services.AuthService;
import com.project.smartcampus.services.NotificationService;
import com.project.smartcampus.util.HttpUtil;
import com.project.smartcampus.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class NotificationController {
    private final AuthService authService;
    private final NotificationService notificationService;

    public NotificationController(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        this.notificationService = notificationService;
    }

    public boolean canHandle(String path) {
        return "/notifications".equals(path) || path.startsWith("/notifications/");
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/notifications".equals(path)) {
            handleList(exchange);
            return;
        }
        if (path.startsWith("/notifications/")) {
            handleItem(exchange, path.substring("/notifications/".length()));
            return;
        }
        HttpUtil.sendJson(exchange, 404, JsonUtil.message("Route not found."));
    }

    private void handleList(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user = authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            String readParam = queryParam(exchange, "read");
            Boolean readFilter = readParam == null ? null : Boolean.parseBoolean(readParam);
            List<NotificationDTO> notifications = notificationService.listNotifications(user.email(), readFilter);
            HttpUtil.sendJson(exchange, 200, JsonUtil.notifications(notifications));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleItem(HttpExchange exchange, String notificationId) throws IOException {
        try {
            AuthService.AuthenticatedUser user = authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            if ("PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = HttpUtil.readBody(exchange);
                Boolean read = JsonUtil.extractBoolean(body, "read");
                if (read == null) {
                    HttpUtil.sendJson(exchange, 400, JsonUtil.message("Field `read` is required."));
                    return;
                }
                NotificationDTO updated = notificationService.markNotification(user.email(), notificationId, read);
                HttpUtil.sendJson(exchange, 200, JsonUtil.notification(updated));
                return;
            }

            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                notificationService.deleteNotification(user.email(), notificationId);
                HttpUtil.sendJson(exchange, 200, JsonUtil.message("Notification deleted."));
                return;
            }

            HttpUtil.sendMethodNotAllowed(exchange);
        } catch (IllegalArgumentException exception) {
            HttpUtil.sendJson(exchange, 404, JsonUtil.message(exception.getMessage()));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private String queryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String part : query.split("&")) {
            String[] segments = part.split("=", 2);
            if (segments.length == 2 && key.equals(segments[0])) {
                return segments[1];
            }
        }
        return null;
    }
}
