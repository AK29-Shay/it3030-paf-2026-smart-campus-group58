package com.project.smartcampus.controller;

import com.project.smartcampus.dto.CommandCenterResponse;
import com.project.smartcampus.services.CommandCenterService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCommandCenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommandCenterService commandCenterService;

    @Test
    void getCommandCenter_withAdminRole_shouldReturnSnapshot() throws Exception {
        when(commandCenterService.getSnapshot()).thenReturn(CommandCenterResponse.builder()
                .generatedAt(LocalDateTime.now())
                .metrics(CommandCenterResponse.Metrics.builder()
                        .totalResources(2)
                        .activeResources(1)
                        .outOfServiceResources(1)
                        .totalBookings(3)
                        .pendingBookings(1)
                        .totalTickets(2)
                        .openTickets(1)
                        .totalUsers(4)
                        .build())
                .resourceDemand(List.of())
                .bookingTrend(List.of())
                .riskAlerts(List.of(CommandCenterResponse.RiskAlert.builder()
                        .severity("INFO")
                        .title("Campus operations stable")
                        .message("No urgent risks.")
                        .action("Monitor")
                        .build()))
                .slaWatchlist(List.of())
                .build());

        mockMvc.perform(get("/api/admin/command-center").with(user("1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.totalResources").value(2))
                .andExpect(jsonPath("$.riskAlerts[0].severity").value("INFO"));
    }

    @Test
    void getCommandCenter_withUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/command-center").with(user("2").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCommandCenter_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/command-center"))
                .andExpect(status().isUnauthorized());
    }
}
