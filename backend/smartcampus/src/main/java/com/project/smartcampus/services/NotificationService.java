package com.project.smartcampus.services;

import com.project.smartcampus.dto.NotificationDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {
    private final Map<String, LinkedHashMap<String, NotificationEntry>> notificationsByEmail = new ConcurrentHashMap<>();

    public void seedDefaults(String userEmail, String adminEmail) {
        ensureUser(userEmail);
        ensureUser(adminEmail);
        addNotification(userEmail, "Your Smart Campus account is ready.");
        addNotification(userEmail, "Your next booking update will appear here.");
        addNotification(adminEmail, "Admin dashboard access is active.");
    }

    public void ensureUser(String email) {
        notificationsByEmail.computeIfAbsent(normalize(email), ignored -> new LinkedHashMap<>());
    }

    public List<NotificationDTO> listNotifications(String email, Boolean readFilter) {
        ensureUser(email);
        List<NotificationDTO> notifications = new ArrayList<>();
        for (NotificationEntry entry : notificationsByEmail.get(normalize(email)).values()) {
            if (readFilter == null || entry.read() == readFilter) {
                notifications.add(new NotificationDTO(entry.id(), entry.message(), entry.read(), entry.createdAt()));
            }
        }
        return notifications;
    }

    public NotificationDTO markNotification(String email, String id, boolean read) {
        ensureUser(email);
        NotificationEntry entry = notificationsByEmail.get(normalize(email)).get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Notification not found.");
        }

        NotificationEntry updated = new NotificationEntry(entry.id(), entry.message(), read, entry.createdAt());
        notificationsByEmail.get(normalize(email)).put(id, updated);
        return new NotificationDTO(updated.id(), updated.message(), updated.read(), updated.createdAt());
    }

    public void deleteNotification(String email, String id) {
        ensureUser(email);
        NotificationEntry removed = notificationsByEmail.get(normalize(email)).remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("Notification not found.");
        }
    }

    public int totalNotifications(String email) {
        ensureUser(email);
        return notificationsByEmail.get(normalize(email)).size();
    }

    public int unreadNotifications(String email) {
        ensureUser(email);
        int unread = 0;
        for (NotificationEntry entry : notificationsByEmail.get(normalize(email)).values()) {
            if (!entry.read()) {
                unread++;
            }
        }
        return unread;
    }

    public void addWelcomeNotification(String email, String name) {
        ensureUser(email);
        addNotification(email, "Welcome to Smart Campus, " + name + ".");
    }

    private void addNotification(String email, String message) {
        ensureUser(email);
        NotificationEntry entry = new NotificationEntry(UUID.randomUUID().toString(), message, false, Instant.now().toString());
        notificationsByEmail.get(normalize(email)).put(entry.id(), entry);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record NotificationEntry(String id, String message, boolean read, String createdAt) {
    }
}
