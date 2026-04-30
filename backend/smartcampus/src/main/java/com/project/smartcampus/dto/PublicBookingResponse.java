package com.project.smartcampus.dto;

import java.time.LocalDateTime;

import com.project.smartcampus.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicBookingResponse {
    private Long id;
    private String resourceName;
    private String purpose;
    private int attendees;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime checkedInTime;
}
