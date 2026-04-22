package com.project.smartcampus.services;

import com.project.smartcampus.dto.NotificationDTO;
import com.project.smartcampus.exception.ResourceNotFoundException;
import com.project.smartcampus.enums.NotificationType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NotificationService {
    private final Map<String, LinkedHashMap<String, NotificationDTO>> notificationsByRecipient = new ConcurrentHashMap<>();
    private final AtomicLong nextNotificationId = new AtomicLong(1);

    public void seedDefaults(String userEmail, String adminEmail) {
        ensureUser(userEmail);
        ensureUser(adminEmail);
        addNotification(userEmail, null, "Welcome", "Your Smart Campus account is ready.", null, null);
        addNotification(userEmail, null, "Booking Updates", "Your next booking update will appear here.", null, null);
        addNotification(adminEmail, null, "Admin Access", "Admin dashboard access is active.", null, null);
    }

    public void ensureUser(String email) {
        ensureRecipient(recipientKey(email));
    }

    public List<NotificationDTO> listNotifications(String email, Boolean readFilter) {
        return filterNotifications(recipientKey(email), readFilter);
    }

    public NotificationDTO markNotification(String email, String id, boolean read) {
        return markNotificationInternal(recipientKey(email), id, read);
    }

    public void deleteNotification(String email, String id) {
        deleteNotificationInternal(recipientKey(email), id);
    }

    public int totalNotifications(String email) {
        ensureUser(email);
        return notificationsByRecipient.get(recipientKey(email)).size();
    }

    public int unreadNotifications(String email) {
        return filterNotifications(recipientKey(email), false).size();
    }

    public void addWelcomeNotification(String email, String name) {
        addNotification(email, NotificationType.ROLE_CHANGED, "Welcome", "Welcome to Smart Campus, " + name + ".", null, null);
    }

    public NotificationDTO createNotification(
            Long recipientId,
            NotificationType type,
            String title,
            String message,
            Long referenceId,
            String referenceType) {
        NotificationDTO notification = createNotificationRecord(
                String.valueOf(nextNotificationId.getAndIncrement()),
                recipientId,
                type,
                title,
                message,
                referenceId,
                referenceType);
        storeNotification(recipientKey(recipientId), notification);
        return notification;
    }

    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        return filterNotifications(recipientKey(userId), null);
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return filterNotifications(recipientKey(userId), false);
    }

    public long getUnreadCount(Long userId) {
        return getUnreadNotifications(userId).size();
    }

    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        return markNotificationInternal(recipientKey(userId), String.valueOf(notificationId), true);
    }

    public void markAllAsRead(Long userId) {
        markAllAsReadKey(recipientKey(userId));
    }

    public void markAllAsRead(String email) {
        markAllAsReadKey(recipientKey(email));
    }

    public void deleteNotification(Long notificationId, Long userId) {
        deleteNotificationInternal(recipientKey(userId), String.valueOf(notificationId));
    }

    public void clearAllNotifications(Long userId) {
        clearAllNotificationsKey(recipientKey(userId));
    }

    public void clearAllNotifications(String email) {
        clearAllNotificationsKey(recipientKey(email));
    }

    public void notifyBookingApproved(Long userId, Long bookingId, String resourceName) {
        createNotification(
                userId,
                NotificationType.BOOKING_APPROVED,
                "Booking Approved",
                "Your booking for \"" + resourceName + "\" has been approved.",
                bookingId,
                "BOOKING");
    }

    public void notifyBookingRejected(Long userId, Long bookingId, String resourceName, String reason) {
        createNotification(
                userId,
                NotificationType.BOOKING_REJECTED,
                "Booking Rejected",
                "Your booking for \"" + resourceName + "\" was rejected. Reason: " + reason,
                bookingId,
                "BOOKING");
    }

    public void notifyTicketStatusChanged(Long userId, Long ticketId, String newStatus) {
        createNotification(
                userId,
                NotificationType.TICKET_STATUS_CHANGED,
                "Ticket Status Updated",
                "Your ticket status has been changed to: " + newStatus,
                ticketId,
                "TICKET");
    }

    public void notifyTicketAssigned(Long technicianId, Long ticketId, String ticketTitle) {
        createNotification(
                technicianId,
                NotificationType.TICKET_ASSIGNED,
                "New Ticket Assigned",
                "You have been assigned to ticket: \"" + ticketTitle + "\"",
                ticketId,
                "TICKET");
    }

    public void notifyNewComment(Long userId, Long ticketId, String commenterName) {
        createNotification(
                userId,
                NotificationType.NEW_COMMENT,
                "New Comment on Your Ticket",
                commenterName + " added a comment to your ticket.",
                ticketId,
                "TICKET");
    }

    private NotificationDTO createNotificationRecord(
            String id,
            Long recipientId,
            NotificationType type,
            String title,
            String message,
            Long referenceId,
            String referenceType) {
        return new NotificationDTO(
                id,
                recipientId,
                type,
                title,
                message,
                referenceId,
                referenceType,
                false,
                Instant.now().toString());
    }

    private void addNotification(
            String email,
            NotificationType type,
            String title,
            String message,
            Long referenceId,
            String referenceType) {
        NotificationDTO notification = createNotificationRecord(
                String.valueOf(nextNotificationId.getAndIncrement()),
                null,
                type,
                title,
                message,
                referenceId,
                referenceType);
        storeNotification(recipientKey(email), notification);
    }

    private List<NotificationDTO> filterNotifications(String key, Boolean readFilter) {
        ensureRecipient(key);
        List<NotificationDTO> notifications = new ArrayList<>();
        for (NotificationDTO notification : notificationsByRecipient.get(key).values()) {
            if (readFilter == null || notification.isRead() == readFilter) {
                notifications.add(notification);
            }
        }
        return notifications;
    }

    private NotificationDTO markNotificationInternal(String key, String id, boolean read) {
        ensureRecipient(key);
        NotificationDTO existing = notificationsByRecipient.get(key).get(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        NotificationDTO updated = new NotificationDTO(
                existing.getId(),
                existing.getRecipientId(),
                existing.getType(),
                existing.getTitle(),
                existing.getMessage(),
                existing.getReferenceId(),
                existing.getReferenceType(),
                read,
                existing.getCreatedAt());
        storeNotification(key, updated);
        return updated;
    }

    private void deleteNotificationInternal(String key, String id) {
        ensureRecipient(key);
        NotificationDTO removed = notificationsByRecipient.get(key).remove(id);
        if (removed == null) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
    }

    private void markAllAsReadKey(String key) {
        ensureRecipient(key);
        List<NotificationDTO> existing = new ArrayList<>(notificationsByRecipient.get(key).values());
        for (NotificationDTO notification : existing) {
            notificationsByRecipient.get(key).put(
                    notification.getId(),
                    new NotificationDTO(
                            notification.getId(),
                            notification.getRecipientId(),
                            notification.getType(),
                            notification.getTitle(),
                            notification.getMessage(),
                            notification.getReferenceId(),
                            notification.getReferenceType(),
                            true,
                            notification.getCreatedAt()));
        }
    }

    private void clearAllNotificationsKey(String key) {
        ensureRecipient(key);
        notificationsByRecipient.get(key).clear();
    }

    private void storeNotification(String key, NotificationDTO notification) {
        ensureRecipient(key);
        notificationsByRecipient.get(key).put(notification.getId(), notification);
    }

    private void ensureRecipient(String key) {
        notificationsByRecipient.computeIfAbsent(key, ignored -> new LinkedHashMap<>());
    }

    private String recipientKey(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String recipientKey(Long userId) {
        return userId == null ? "" : "user:" + userId;
    }
}
