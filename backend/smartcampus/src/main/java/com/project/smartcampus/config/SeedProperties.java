package com.project.smartcampus.config;

import java.util.Properties;

public record SeedProperties(
        int port,
        String jwtSecret,
        long jwtExpirationSeconds,
        String userName,
        String userEmail,
        String userPassword,
        String adminName,
        String adminEmail,
        String adminPassword
) {
    public static SeedProperties from(Properties properties) {
        return new SeedProperties(
                Integer.parseInt(properties.getProperty("server.port", "8080")),
                properties.getProperty("auth.jwt.secret", "smart-campus-local-secret"),
                Long.parseLong(properties.getProperty("auth.jwt.expiration-seconds", "86400")),
                properties.getProperty("auth.seed.user.name", "Group 58 Student"),
                properties.getProperty("auth.seed.user.email", "student@example.com"),
                properties.getProperty("auth.seed.user.password", "ChangeMe123!"),
                properties.getProperty("auth.seed.admin.name", "Group 58 Admin"),
                properties.getProperty("auth.seed.admin.email", "admin@example.com"),
                properties.getProperty("auth.seed.admin.password", "ChangeMe123!"));
    }
}
