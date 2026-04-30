package com.project.smartcampus.services;

import com.project.smartcampus.dto.CommandCenterResponse;
import com.project.smartcampus.entity.Booking;
import com.project.smartcampus.entity.Resource;
import com.project.smartcampus.entity.Ticket;
import com.project.smartcampus.enums.BookingStatus;
import com.project.smartcampus.enums.ResourceCategory;
import com.project.smartcampus.enums.ResourceStatus;
import com.project.smartcampus.enums.ResourceType;
import com.project.smartcampus.enums.TicketPriority;
import com.project.smartcampus.enums.TicketStatus;
import com.project.smartcampus.repository.BookingRepository;
import com.project.smartcampus.repository.ResourceRepository;
import com.project.smartcampus.repository.TicketRepository;
import com.project.smartcampus.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandCenterServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommandCenterService commandCenterService;

    @Test
    void getSnapshot_shouldAggregateOperationalMetricsAndRisks() {
        Booking pendingBooking = booking("Lab A", BookingStatus.PENDING, LocalDateTime.now());
        Booking approvedBooking = booking("Lab A", BookingStatus.APPROVED, LocalDateTime.now().minusDays(1));
        Booking checkedInBooking = booking("Auditorium", BookingStatus.CHECKED_IN, LocalDateTime.now().minusDays(2));

        Ticket breachedTicket = ticket(10L, "Projector issue", TicketStatus.OPEN, TicketPriority.HIGH,
                LocalDateTime.now().minusHours(12));
        Ticket resolvedTicket = ticket(11L, "Network issue", TicketStatus.RESOLVED, TicketPriority.MEDIUM,
                LocalDateTime.now().minusHours(4));

        when(bookingRepository.findAll()).thenReturn(List.of(pendingBooking, approvedBooking, checkedInBooking));
        when(ticketRepository.findAll()).thenReturn(List.of(breachedTicket, resolvedTicket));
        when(resourceRepository.findAll()).thenReturn(List.of(
                resource("Lab A", ResourceStatus.ACTIVE),
                resource("Auditorium", ResourceStatus.OUT_OF_SERVICE)
        ));
        when(userRepository.count()).thenReturn(4L);

        CommandCenterResponse snapshot = commandCenterService.getSnapshot();

        assertThat(snapshot.getMetrics().getTotalBookings()).isEqualTo(3);
        assertThat(snapshot.getMetrics().getPendingBookings()).isEqualTo(1);
        assertThat(snapshot.getMetrics().getOutOfServiceResources()).isEqualTo(1);
        assertThat(snapshot.getMetrics().getTotalUsers()).isEqualTo(4);
        assertThat(snapshot.getResourceDemand()).first()
                .extracting(CommandCenterResponse.ResourceDemand::getResourceName)
                .isEqualTo("Lab A");
        assertThat(snapshot.getSlaWatchlist()).hasSize(1);
        assertThat(snapshot.getSlaWatchlist().get(0).isBreached()).isTrue();
        assertThat(snapshot.getRiskAlerts())
                .anySatisfy(alert -> assertThat(alert.getTitle()).contains("Booking queue"));
    }

    private Booking booking(String resourceName, BookingStatus status, LocalDateTime startTime) {
        Booking booking = new Booking();
        booking.setResourceName(resourceName);
        booking.setStatus(status);
        booking.setStartTime(startTime);
        return booking;
    }

    private Ticket ticket(Long id,
                          String title,
                          TicketStatus status,
                          TicketPriority priority,
                          LocalDateTime createdAt) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle(title);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCreatedAt(createdAt);
        return ticket;
    }

    private Resource resource(String name, ResourceStatus status) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setType(ResourceType.ROOM);
        resource.setCategory(ResourceCategory.LAB);
        resource.setLocation("Main Building");
        resource.setStatus(status);
        return resource;
    }
}
