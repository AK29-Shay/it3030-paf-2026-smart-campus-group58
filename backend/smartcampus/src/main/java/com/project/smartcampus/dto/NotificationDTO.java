package com.project.smartcampus.dto;

import com.project.smartcampus.enums.NotificationType;

public class NotificationDTO {
    private final String id;
    private final Long recipientId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long referenceId;
    private final String referenceType;
    private final boolean read;
    private final String createdAt;

    public NotificationDTO(String id, String message, boolean read, String createdAt) {
        this(id, null, null, null, message, null, null, read, createdAt);
    }

    public NotificationDTO(
            String id,
            Long recipientId,
            NotificationType type,
            String title,
            String message,
            Long referenceId,
            String referenceType,
            boolean read,
            String createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static NotificationDTO fromNotification(Object notification) {
        Object recipient = invoke(notification, "getRecipient");
        return new NotificationDTO(
                asString(invoke(notification, "getId")),
                asLong(invoke(recipient, "getId")),
                asNotificationType(invoke(notification, "getType")),
                asString(invoke(notification, "getTitle")),
                asString(invoke(notification, "getMessage")),
                asLong(invoke(notification, "getReferenceId")),
                asString(invoke(notification, "getReferenceType")),
                asBoolean(invoke(notification, "isRead"), invoke(notification, "getRead"), invoke(notification, "getIsRead")),
                asString(invoke(notification, "getCreatedAt")));
    }

    public String id() {
        return id;
    }

    public String message() {
        return message;
    }

    public boolean read() {
        return read;
    }

    public String createdAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public boolean isRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    private static Object invoke(Object target, String methodName) {
        if (target == null) {
            return null;
        }

        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean asBoolean(Object... values) {
        for (Object value : values) {
            if (value instanceof Boolean bool) {
                return bool;
            }
            if (value != null) {
                return Boolean.parseBoolean(String.valueOf(value));
            }
        }
        return false;
    }

    private static NotificationType asNotificationType(Object value) {
        if (value instanceof NotificationType type) {
            return type;
        }
        if (value == null) {
            return null;
        }
        try {
            return NotificationType.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
