package com.project.smartcampus.controller;

import com.project.smartcampus.dto.BookingResponse;
import com.project.smartcampus.dto.PublicBookingResponse;
import com.project.smartcampus.enums.BookingStatus;
import com.project.smartcampus.services.BookingService;
import com.project.smartcampus.services.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;

    @Test
    void createBooking_withAuthenticatedUser_shouldReturn201() throws Exception {
        when(userService.extractUserEmail(ArgumentMatchers.any())).thenReturn("student@example.com");
        when(bookingService.createBooking(ArgumentMatchers.any())).thenReturn(sampleBooking());

        String payload = """
                {
                  "resourceName": "Lab A",
                  "purpose": "Study session",
                  "attendees": 12,
                  "startTime": "2026-05-10T09:00:00",
                  "endTime": "2026-05-10T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .with(user("2").roles("USER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void deleteBooking_withAuthenticatedUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/bookings/7").with(user("2").roles("USER")).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicBookingLookup_withoutAuthentication_shouldReturn200() throws Exception {
        when(bookingService.getPublicBookingById(7L)).thenReturn(PublicBookingResponse.builder()
                .id(7L)
                .resourceName("Lab A")
                .purpose("Study session")
                .attendees(12)
                .status(BookingStatus.APPROVED)
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .build());

        mockMvc.perform(get("/api/bookings/public/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceName").value("Lab A"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void bookingList_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkIn_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/bookings/checkin/7"))
                .andExpect(status().isUnauthorized());
    }

    private BookingResponse sampleBooking() {
        BookingResponse response = new BookingResponse();
        response.setId(7L);
        response.setResourceName("Lab A");
        response.setPurpose("Study session");
        response.setBookedBy("student@example.com");
        response.setAttendees(12);
        response.setStatus(BookingStatus.PENDING);
        response.setStartTime(LocalDateTime.of(2026, 5, 10, 9, 0));
        response.setEndTime(LocalDateTime.of(2026, 5, 10, 10, 0));
        return response;
    }
}
