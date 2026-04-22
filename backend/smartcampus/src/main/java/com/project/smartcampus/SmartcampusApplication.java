package com.project.smartcampus;

import com.project.smartcampus.config.JwtUtil;
import com.project.smartcampus.config.SeedProperties;
import com.project.smartcampus.controller.AuthController;
import com.project.smartcampus.controller.HomeController;
import com.project.smartcampus.controller.NotificationController;
import com.project.smartcampus.controller.UserController;
import com.project.smartcampus.services.AuthService;
import com.project.smartcampus.services.NotificationService;
import com.project.smartcampus.util.HttpUtil;
import com.project.smartcampus.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Executors;

public class SmartcampusApplication {
    public static void main(String[] args) throws Exception {
        SeedProperties seedProperties = SeedProperties.from(loadProperties());
        JwtUtil jwtUtil = new JwtUtil(seedProperties.jwtSecret(), seedProperties.jwtExpirationSeconds());
        AuthService authService = new AuthService(seedProperties, jwtUtil);
        NotificationService notificationService = new NotificationService();
        notificationService.seedDefaults(seedProperties.userEmail(), seedProperties.adminEmail());

        AuthController authController = new AuthController(authService, notificationService);
        UserController userController = new UserController(authService);
        NotificationController notificationController = new NotificationController(authService, notificationService);
        HomeController homeController = new HomeController(authService, notificationService);

        HttpServer server = HttpServer.create(new InetSocketAddress(seedProperties.port()), 0);
        server.createContext("/", exchange -> route(exchange, authController, userController, notificationController, homeController));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Smart Campus auth demo running on http://localhost:" + seedProperties.port());
        System.out.println("Seed user: " + seedProperties.userEmail() + " / " + seedProperties.userPassword());
        System.out.println("Seed admin: " + seedProperties.adminEmail() + " / " + seedProperties.adminPassword());
    }

    private static void route(
            HttpExchange exchange,
            AuthController authController,
            UserController userController,
            NotificationController notificationController,
            HomeController homeController) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            if (authController.canHandle(path)) {
                authController.handle(exchange);
                return;
            }
            if (userController.canHandle(path)) {
                userController.handle(exchange);
                return;
            }
            if (notificationController.canHandle(path)) {
                notificationController.handle(exchange);
                return;
            }
            if (homeController.canHandle(path)) {
                homeController.handle(exchange);
                return;
            }
            HttpUtil.sendJson(exchange, 404, JsonUtil.message("Route not found."));
        } catch (Exception exception) {
            HttpUtil.sendJson(exchange, 500, JsonUtil.message("Server error: " + exception.getMessage()));
        }
    }

    private static Properties loadProperties() throws IOException {
        Path[] candidates = new Path[] {
                Path.of("backend", "smartcampus", "src", "main", "resources", "application.properties"),
                Path.of("src", "main", "resources", "application.properties")
        };

        Properties properties = new Properties();
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                try (InputStream inputStream = Files.newInputStream(candidate)) {
                    properties.load(inputStream);
                    return properties;
                }
            }
        }
        return properties;
    }
}
