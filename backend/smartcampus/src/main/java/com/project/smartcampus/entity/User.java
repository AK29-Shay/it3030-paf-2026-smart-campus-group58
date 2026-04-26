package com.project.smartcampus.entity;

import com.project.smartcampus.enums.Role;
import com.project.smartcampus.enums.TechnicianSpecialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entity representing a user in the Smart Campus system.
 * Users are registered via Google OAuth2 sign-in.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String email;

    private String name;

    private String profilePicture;

    @Builder.Default
    private Role role = Role.USER;

    private TechnicianSpecialty technicianSpecialty;

    private String provider;

    private String providerId;

    private String passwordHash;

    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    @Builder.Default
    private Boolean notificationsEnabled = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
