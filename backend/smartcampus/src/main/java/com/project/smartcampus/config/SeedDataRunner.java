package com.project.smartcampus.config;

import com.project.smartcampus.entity.User;
import com.project.smartcampus.entity.Resource;
import com.project.smartcampus.enums.ResourceCategory;
import com.project.smartcampus.enums.ResourceStatus;
import com.project.smartcampus.enums.ResourceType;
import com.project.smartcampus.enums.Role;
import com.project.smartcampus.repository.ResourceRepository;
import com.project.smartcampus.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.Locale;

/**
 * Creates default local privileged accounts at startup when enabled.
 */
@Slf4j
@Component
public class SeedDataRunner implements CommandLineRunner {

    private static final String LOCAL_PROVIDER = "LOCAL";

    private final SeedProperties seedProperties;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(SeedProperties seedProperties,
                          UserRepository userRepository,
                          ResourceRepository resourceRepository,
                          PasswordEncoder passwordEncoder) {
        this.seedProperties = seedProperties;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.isEnabled()) {
            log.debug("Seed data tooling is disabled.");
            return;
        }

        validateRequiredSeedSettings();

        seedUser(
                Role.ADMIN,
                seedProperties.getAdminName(),
                seedProperties.getAdminEmail(),
                seedProperties.getAdminPassword()
        );

        seedUser(
                Role.USER,
                seedProperties.getUserName(),
                seedProperties.getUserEmail(),
                seedProperties.getUserPassword()
        );

        seedUser(
                Role.TECHNICIAN,
                seedProperties.getTechnicianName(),
                seedProperties.getTechnicianEmail(),
                seedProperties.getTechnicianPassword()
        );

        seedResources();
    }

    private void validateRequiredSeedSettings() {
        validateRequired(seedProperties.getAdminName(), "app.seed.admin-name");
        validateRequired(seedProperties.getAdminEmail(), "app.seed.admin-email");
        validateRequired(seedProperties.getAdminPassword(), "app.seed.admin-password");

        validateRequired(seedProperties.getUserName(), "app.seed.user-name");
        validateRequired(seedProperties.getUserEmail(), "app.seed.user-email");
        validateRequired(seedProperties.getUserPassword(), "app.seed.user-password");

        validateRequired(seedProperties.getTechnicianName(), "app.seed.technician-name");
        validateRequired(seedProperties.getTechnicianEmail(), "app.seed.technician-email");
        validateRequired(seedProperties.getTechnicianPassword(), "app.seed.technician-password");
    }

    private void validateRequired(String value, String key) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Missing required seed configuration: " + key);
        }
    }

    private void seedUser(Role expectedRole, String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmail(normalizedEmail).ifPresentOrElse(existing -> {
            if (existing.getRole() != expectedRole) {
                log.warn(
                        "Seed account {} already exists with role {} (expected {}). Leaving unchanged.",
                        normalizedEmail,
                        existing.getRole(),
                        expectedRole
                );
            } else {
                log.info("Seed account already exists, skipping: {} ({})", normalizedEmail, expectedRole);
            }
        }, () -> {
            User seededUser = User.builder()
                    .id(MongoIdGenerator.nextId())
                    .name(name.trim())
                    .email(normalizedEmail)
                    .role(expectedRole)
                    .provider(LOCAL_PROVIDER)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .notificationsEnabled(true)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            userRepository.save(seededUser);
            log.info("Seeded {} account: {}", expectedRole, normalizedEmail);
        });
    }

    private void seedResources() {
        seedResource(
                "Auditorium A",
                ResourceType.FACILITY,
                ResourceCategory.AUDITORIUM,
                180,
                "Main Academic Building - Ground Floor",
                "Large auditorium for orientation sessions, seminars, and viva rehearsals."
        );
        seedResource(
                "Computer Lab 3",
                ResourceType.FACILITY,
                ResourceCategory.LAB,
                45,
                "Computing Faculty - Level 2",
                "Networked lab with workstations and projector support."
        );
        seedResource(
                "Meeting Room B",
                ResourceType.FACILITY,
                ResourceCategory.MEETING_ROOM,
                12,
                "Library Block - Level 1",
                "Compact discussion room for group project meetings."
        );
        seedResource(
                "Portable Projector P1",
                ResourceType.EQUIPMENT,
                ResourceCategory.PROJECTOR,
                1,
                "IT Help Desk",
                "Borrowable projector for classroom and presentation use."
        );
    }

    private void seedResource(String name,
                              ResourceType type,
                              ResourceCategory category,
                              int capacity,
                              String location,
                              String description) {
        resourceRepository.findFirstByNameIgnoreCase(name).ifPresentOrElse(existing -> {
            log.info("Seed resource already exists, skipping: {}", name);
        }, () -> {
            Resource resource = new Resource();
            resource.setId(MongoIdGenerator.nextId());
            resource.setName(name);
            resource.setType(type);
            resource.setCategory(category);
            resource.setCapacity(capacity);
            resource.setLocation(location);
            resource.setAvailabilityStart(LocalTime.of(8, 0));
            resource.setAvailabilityEnd(LocalTime.of(18, 0));
            resource.setDescription(description);
            resource.setStatus(ResourceStatus.ACTIVE);

            resourceRepository.save(resource);
            log.info("Seeded resource: {}", name);
        });
    }
}
