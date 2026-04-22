package com.project.smartcampus.util;

import com.project.smartcampus.dto.AuthResponse;
import com.project.smartcampus.dto.NotificationDTO;
import com.project.smartcampus.services.AuthService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String extractString(String body, String field) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(body == null ? "" : body);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static Boolean extractBoolean(String body, String field) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(body == null ? "" : body);
        if (!matcher.find()) {
            return null;
        }
        return Boolean.parseBoolean(matcher.group(1));
    }

    public static String authResponse(AuthResponse response) {
        return "{"
                + "\"accessToken\":\"" + escape(response.accessToken()) + "\","
                + "\"email\":\"" + escape(response.email()) + "\","
                + "\"role\":\"" + response.role().name() + "\""
                + "}";
    }

    public static String userResponse(AuthService.AuthenticatedUser user) {
        return "{"
                + "\"name\":\"" + escape(user.name()) + "\","
                + "\"email\":\"" + escape(user.email()) + "\","
                + "\"role\":\"" + user.role().name() + "\""
                + "}";
    }

    public static String notification(NotificationDTO notification) {
        return "{"
                + "\"id\":\"" + escape(notification.id()) + "\","
                + "\"message\":\"" + escape(notification.message()) + "\","
                + "\"read\":" + notification.read() + ","
                + "\"createdAt\":\"" + escape(notification.createdAt()) + "\""
                + "}";
    }

    public static String notifications(List<NotificationDTO> notifications) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"items\":[");
        for (int index = 0; index < notifications.size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(notification(notifications.get(index)));
        }
        builder.append("]}");
        return builder.toString();
    }

    public static String message(String message) {
        return "{\"message\":\"" + escape(message) + "\"}";
    }

    public static String health() {
        return "{\"status\":\"UP\"}";
    }

    public static String adminSummary(int totalUsers, int totalNotifications, int unreadNotifications) {
        return "{"
                + "\"totalUsers\":" + totalUsers + ","
                + "\"totalNotifications\":" + totalNotifications + ","
                + "\"unreadNotifications\":" + unreadNotifications
                + "}";
    }

    public static String escape(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
