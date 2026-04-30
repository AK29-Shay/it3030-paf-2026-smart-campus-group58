package com.project.smartcampus.controller;

import com.project.smartcampus.dto.CommandCenterResponse;
import com.project.smartcampus.services.CommandCenterService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminCommandCenterController {

    private final CommandCenterService commandCenterService;

    public AdminCommandCenterController(CommandCenterService commandCenterService) {
        this.commandCenterService = commandCenterService;
    }

    @GetMapping("/command-center")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommandCenterResponse> getCommandCenter() {
        return ResponseEntity.ok(commandCenterService.getSnapshot());
    }
}
