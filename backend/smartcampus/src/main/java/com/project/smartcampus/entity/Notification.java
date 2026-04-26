package com.project.smartcampus.entity;

import com.project.smartcampus.enums.NotificationType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to a user.
 */
@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private Long id;

    private Long recipientId;

    private NotificationType type;

    private String title;

    @Size(max = 500)
    private String message;

    private Long referenceId;

    private String referenceType;

    @Builder.Default
    private boolean isRead = false;

    private LocalDateTime createdAt;
}
