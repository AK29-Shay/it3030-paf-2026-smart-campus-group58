package com.project.smartcampus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandCenterResponse {

    private LocalDateTime generatedAt;
    private Metrics metrics;
    private List<ResourceDemand> resourceDemand;
    private List<BookingTrend> bookingTrend;
    private List<RiskAlert> riskAlerts;
    private List<SlaWatchlistItem> slaWatchlist;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private long totalResources;
        private long activeResources;
        private long outOfServiceResources;
        private long totalBookings;
        private long pendingBookings;
        private long approvedBookings;
        private long rejectedBookings;
        private long checkedInBookings;
        private long totalTickets;
        private long openTickets;
        private long inProgressTickets;
        private long resolvedTickets;
        private long closedTickets;
        private long totalUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceDemand {
        private String resourceName;
        private String status;
        private long totalBookings;
        private long pendingBookings;
        private long approvedBookings;
        private long checkedInBookings;
        private int utilizationScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingTrend {
        private LocalDate date;
        private String label;
        private long totalBookings;
        private long pendingBookings;
        private long approvedBookings;
        private long rejectedBookings;
        private long checkedInBookings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAlert {
        private String severity;
        private String title;
        private String message;
        private String action;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaWatchlistItem {
        private Long ticketId;
        private String title;
        private String status;
        private String priority;
        private Long assignedTo;
        private long ageHours;
        private long targetHours;
        private boolean breached;
    }
}
