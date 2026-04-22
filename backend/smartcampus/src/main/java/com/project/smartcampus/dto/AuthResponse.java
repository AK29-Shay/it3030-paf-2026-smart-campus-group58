package com.project.smartcampus.dto;

import com.project.smartcampus.enums.Role;

public record AuthResponse(String accessToken, String email, Role role) {
}
