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
        return "/notifications".equals(path)
                || path.startsWith("/notifications/")
                || "/api/notifications".equals(path)
                || path.startsWith("/api/notifications/");
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        if ("/notifications".equals(path)) {
            handleList(exchange, null);
            return;
        }
        if ("/notifications/unread".equals(path)) {
            handleList(exchange, false);
            return;
        }
        if ("/notifications/unread/count".equals(path)) {
            handleUnreadCount(exchange);
            return;
        }
        if ("/notifications/read-all".equals(path)) {
            handleReadAll(exchange);
            return;
        }
        if ("/notifications/clear".equals(path)) {
            handleClear(exchange);
            return;
        }
        if (path.startsWith("/notifications/") && path.endsWith("/read")) {
            String notificationId = path.substring("/notifications/".length(), path.length() - "/read".length());
            handleMarkRead(exchange, notificationId);
            return;
        }
        if (path.startsWith("/notifications/")) {
            handleItem(exchange, path.substring("/notifications/".length()));
            return;
        }
        HttpUtil.sendJson(exchange, 404, JsonUtil.message("Route not found."));
    }

    private void handleList(HttpExchange exchange, Boolean readFilter) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            if (readFilter == null) {
                String readParam = queryParam(exchange, "read");
                readFilter = readParam == null ? null : Boolean.parseBoolean(readParam);
            }
            List<NotificationDTO> notifications = notificationService.listNotifications(user.email(), readFilter);
            HttpUtil.sendJson(exchange, 200, JsonUtil.notifications(notifications));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleUnreadCount(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            int unread = notificationService.unreadNotifications(user.email());
            HttpUtil.sendJson(exchange, 200, "{\"count\":" + unread + "}");
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleReadAll(HttpExchange exchange) throws IOException {
        if (!"PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            notificationService.markAllAsRead(user.email());
            HttpUtil.sendJson(exchange, 200, JsonUtil.message("All notifications marked as read."));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleClear(HttpExchange exchange) throws IOException {
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            notificationService.clearAllNotifications(user.email());
            HttpUtil.sendJson(exchange, 200, JsonUtil.message("All notifications cleared."));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleMarkRead(HttpExchange exchange, String notificationId) throws IOException {
        if (!"PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }

        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
            NotificationDTO updated = notificationService.markNotification(user.email(), notificationId, true);
            HttpUtil.sendJson(exchange, 200, JsonUtil.notification(updated));
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        } catch (RuntimeException exception) {
            HttpUtil.sendJson(exchange, 404, JsonUtil.message(exception.getMessage()));
        }
    }

    private void handleItem(HttpExchange exchange, String notificationId) throws IOException {
        try {
            AuthService.AuthenticatedUser user =
                    authService.requireAuthenticated(exchange.getRequestHeaders().getFirst("Authorization"));
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
        } catch (SecurityException exception) {
            HttpUtil.sendJson(exchange, 401, JsonUtil.message(exception.getMessage()));
        } catch (RuntimeException exception) {
            HttpUtil.sendJson(exchange, 404, JsonUtil.message(exception.getMessage()));
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

    private String normalizePath(String path) {
        if (path.startsWith("/api/notifications")) {
            return path.substring("/api".length());
        }
        return path;
    }
}
