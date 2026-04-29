package com.project.smartcampus;

import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SmartcampusApplication {

    private static final String LOCAL_MONGO_URI = "mongodb://127.0.0.1:27017/smartcampus";

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

        setSystemPropertyIfPresent(dotenv, "SERVER_PORT", "server.port");
        configureMongoProperties(dotenv);
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

    private static void configureMongoProperties(Dotenv dotenv) {
        String configuredUri = firstNonBlank(
                dotenv.get("SPRING_DATA_MONGODB_URI"),
                System.getenv("SPRING_DATA_MONGODB_URI"),
                System.getProperty("spring.data.mongodb.uri"),
                System.getProperty("spring.mongodb.uri")
        );

        String resolvedUri = resolveMongoUri(configuredUri);
        System.setProperty("spring.mongodb.uri", resolvedUri);
        System.setProperty("spring.data.mongodb.uri", resolvedUri);
    }

    private static String resolveMongoUri(String configuredUri) {
        if (!hasText(configuredUri)) {
            return LOCAL_MONGO_URI;
        }

        if (isRemoteMongo(configuredUri) && !isMongoReachable(configuredUri) && isMongoReachable(LOCAL_MONGO_URI)) {
            System.out.println("Configured remote MongoDB is not reachable; falling back to local MongoDB.");
            return LOCAL_MONGO_URI;
        }

        return configuredUri;
    }

    private static boolean isRemoteMongo(String mongoUri) {
        return hasText(mongoUri) && (mongoUri.startsWith("mongodb+srv://") || mongoUri.contains("mongodb.net"));
    }

    private static boolean isMongoReachable(String mongoUri) {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(2, TimeUnit.SECONDS))
                .build();

        try (MongoClient client = MongoClients.create(settings)) {
            MongoDatabase database = client.getDatabase("admin");
            database.runCommand(new org.bson.Document("ping", 1));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
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
