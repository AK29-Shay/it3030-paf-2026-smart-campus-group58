package com.project.smartcampus;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartcampusApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

        setSystemPropertyIfPresent(dotenv, "SERVER_PORT", "server.port");
        setSystemPropertyIfPresent(dotenv, "SPRING_DATA_MONGODB_URI", "spring.mongodb.uri");
        setSystemPropertyIfPresent(dotenv, "SPRING_DATA_MONGODB_URI", "spring.data.mongodb.uri");
        setSystemPropertyIfPresent(dotenv, "AUTH_JWT_SECRET", "auth.jwt.secret");
        setSystemPropertyIfPresent(dotenv, "AUTH_JWT_EXPIRATION_SECONDS", "auth.jwt.expiration-seconds");
        setSystemPropertyIfPresent(dotenv, "SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID", "spring.security.oauth2.client.registration.google.client-id");
        setSystemPropertyIfPresent(dotenv, "SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET", "spring.security.oauth2.client.registration.google.client-secret");
        setSystemPropertyIfPresent(dotenv, "APP_FRONTEND_URL", "app.frontend.url");
        setSystemPropertyIfPresent(dotenv, "APP_CORS_ALLOWED_ORIGINS", "app.cors.allowed-origins");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_ENABLED", "app.seed.enabled");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_ADMIN_NAME", "app.seed.admin-name");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_ADMIN_EMAIL", "app.seed.admin-email");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_ADMIN_PASSWORD", "app.seed.admin-password");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_USER_NAME", "app.seed.user-name");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_USER_EMAIL", "app.seed.user-email");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_USER_PASSWORD", "app.seed.user-password");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_TECHNICIAN_NAME", "app.seed.technician-name");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_TECHNICIAN_EMAIL", "app.seed.technician-email");
        setSystemPropertyIfPresent(dotenv, "APP_SEED_TECHNICIAN_PASSWORD", "app.seed.technician-password");

        String mongoUri = System.getProperty("spring.mongodb.uri");
        if (mongoUri == null || mongoUri.isBlank()) {
            mongoUri = System.getProperty("spring.data.mongodb.uri");
        }
        String mongoSource = mongoUri == null || mongoUri.isBlank()
                ? "application defaults"
                : (mongoUri.startsWith("mongodb+srv://") ? "mongodb+srv" : "local mongo");
        System.out.println("Resolved Mongo source = " + mongoSource);
        System.out.println("Resolved server port = " + System.getProperty("server.port"));

        SpringApplication.run(SmartcampusApplication.class, args);
    }

    private static void setSystemPropertyIfPresent(Dotenv dotenv, String dotenvKey, String propertyKey) {
        String value = dotenv.get(dotenvKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(dotenvKey);
        }
        if (value != null && !value.isBlank()) {
            System.setProperty(dotenvKey, value);
            System.setProperty(propertyKey, value);
        }
    }
}
