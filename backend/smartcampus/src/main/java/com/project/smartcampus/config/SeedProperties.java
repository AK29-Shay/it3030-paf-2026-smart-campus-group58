package com.project.smartcampus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Seed account configuration loaded from app.seed.* properties.
 */
@Component
@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {

    private boolean enabled = false;

    private String adminName = "System Admin";
    private String adminEmail = "admin@smartcampus.local";
    private String adminPassword = "AdminPassword123!";

    private String userName = "Group 58 Student";
    private String userEmail = "student@example.com";
    private String userPassword = "ChangeMe123!";

    private String technicianName = "Support Technician";
    private String technicianEmail = "technician@smartcampus.local";
    private String technicianPassword = "TechnicianPassword123!";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getTechnicianEmail() {
        return technicianEmail;
    }

    public void setTechnicianEmail(String technicianEmail) {
        this.technicianEmail = technicianEmail;
    }

    public String getTechnicianPassword() {
        return technicianPassword;
    }

    public void setTechnicianPassword(String technicianPassword) {
        this.technicianPassword = technicianPassword;
    }
}
