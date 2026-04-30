package com.project.smartcampus.services;

import com.project.smartcampus.dto.CommandCenterResponse;
import com.project.smartcampus.entity.Booking;
import com.project.smartcampus.entity.Resource;
import com.project.smartcampus.entity.Ticket;
import com.project.smartcampus.enums.BookingStatus;
import com.project.smartcampus.enums.ResourceStatus;
import com.project.smartcampus.enums.TicketPriority;
import com.project.smartcampus.enums.TicketStatus;
import com.project.smartcampus.repository.BookingRepository;
import com.project.smartcampus.repository.ResourceRepository;
import com.project.smartcampus.repository.TicketRepository;
import com.project.smartcampus.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommandCenterService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public CommandCenterService(BookingRepository bookingRepository,
                                TicketRepository ticketRepository,
                                ResourceRepository resourceRepository,
                                UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    public CommandCenterResponse getSnapshot() {
        List<Booking> bookings = bookingRepository.findAll();
        List<Ticket> tickets = ticketRepository.findAll();
        List<Resource> resources = resourceRepository.findAll();

        CommandCenterResponse.Metrics metrics = buildMetrics(bookings, tickets, resources);
        List<CommandCenterResponse.SlaWatchlistItem> slaWatchlist = buildSlaWatchlist(tickets);

        return CommandCenterResponse.builder()
                .generatedAt(LocalDateTime.now())
                .metrics(metrics)
                .resourceDemand(buildResourceDemand(bookings, resources))
                .bookingTrend(buildBookingTrend(bookings))
                .riskAlerts(buildRiskAlerts(metrics, slaWatchlist))
                .slaWatchlist(slaWatchlist)
                .build();
    }

    private CommandCenterResponse.Metrics buildMetrics(List<Booking> bookings,
                                                       List<Ticket> tickets,
                                                       List<Resource> resources) {
        return CommandCenterResponse.Metrics.builder()
                .totalResources(resources.size())
                .activeResources(countResources(resources, ResourceStatus.ACTIVE))
                .outOfServiceResources(countResources(resources, ResourceStatus.OUT_OF_SERVICE))
                .totalBookings(bookings.size())
                .pendingBookings(countBookings(bookings, BookingStatus.PENDING))
                .approvedBookings(countBookings(bookings, BookingStatus.APPROVED))
                .rejectedBookings(countBookings(bookings, BookingStatus.REJECTED))
                .checkedInBookings(countBookings(bookings, BookingStatus.CHECKED_IN))
                .totalTickets(tickets.size())
                .openTickets(countTickets(tickets, TicketStatus.OPEN))
                .inProgressTickets(countTickets(tickets, TicketStatus.IN_PROGRESS))
                .resolvedTickets(countTickets(tickets, TicketStatus.RESOLVED))
                .closedTickets(countTickets(tickets, TicketStatus.CLOSED))
                .totalUsers(userRepository.count())
                .build();
    }

    private List<CommandCenterResponse.ResourceDemand> buildResourceDemand(List<Booking> bookings,
                                                                           List<Resource> resources) {
        Map<String, Resource> resourcesByName = resources.stream()
                .filter(resource -> resource.getName() != null)
                .collect(Collectors.toMap(
                        resource -> normalizeKey(resource.getName()),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        long maxDemand = Math.max(1, bookings.stream()
                .filter(booking -> hasText(booking.getResourceName()))
                .collect(Collectors.groupingBy(Booking::getResourceName, Collectors.counting()))
                .values()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(1));

        return bookings.stream()
                .filter(booking -> hasText(booking.getResourceName()))
                .collect(Collectors.groupingBy(Booking::getResourceName))
                .entrySet()
                .stream()
                .map(entry -> mapResourceDemand(entry.getKey(), entry.getValue(), resourcesByName, maxDemand))
                .sorted(Comparator.comparingLong(CommandCenterResponse.ResourceDemand::getTotalBookings).reversed()
                        .thenComparing(CommandCenterResponse.ResourceDemand::getResourceName))
                .limit(8)
                .toList();
    }

    private CommandCenterResponse.ResourceDemand mapResourceDemand(String resourceName,
                                                                   List<Booking> bookings,
                                                                   Map<String, Resource> resourcesByName,
                                                                   long maxDemand) {
        Resource resource = resourcesByName.get(normalizeKey(resourceName));
        long total = bookings.size();

        return CommandCenterResponse.ResourceDemand.builder()
                .resourceName(resourceName)
                .status(resource != null && resource.getStatus() != null ? resource.getStatus().name() : "UNKNOWN")
                .totalBookings(total)
                .pendingBookings(countBookings(bookings, BookingStatus.PENDING))
                .approvedBookings(countBookings(bookings, BookingStatus.APPROVED))
                .checkedInBookings(countBookings(bookings, BookingStatus.CHECKED_IN))
                .utilizationScore((int) Math.round((total * 100.0) / maxDemand))
                .build();
    }

    private List<CommandCenterResponse.BookingTrend> buildBookingTrend(List<Booking> bookings) {
        LocalDate today = LocalDate.now();
        List<CommandCenterResponse.BookingTrend> trend = new ArrayList<>();

        for (int offset = 6; offset >= 0; offset -= 1) {
            LocalDate date = today.minusDays(offset);
            List<Booking> dayBookings = bookings.stream()
                    .filter(booking -> booking.getStartTime() != null)
                    .filter(booking -> date.equals(booking.getStartTime().toLocalDate()))
                    .toList();

            trend.add(CommandCenterResponse.BookingTrend.builder()
                    .date(date)
                    .label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .totalBookings(dayBookings.size())
                    .pendingBookings(countBookings(dayBookings, BookingStatus.PENDING))
                    .approvedBookings(countBookings(dayBookings, BookingStatus.APPROVED))
                    .rejectedBookings(countBookings(dayBookings, BookingStatus.REJECTED))
                    .checkedInBookings(countBookings(dayBookings, BookingStatus.CHECKED_IN))
                    .build());
        }

        return trend;
    }

    private List<CommandCenterResponse.RiskAlert> buildRiskAlerts(CommandCenterResponse.Metrics metrics,
                                                                  List<CommandCenterResponse.SlaWatchlistItem> slaWatchlist) {
        List<CommandCenterResponse.RiskAlert> alerts = new ArrayList<>();
        long breachedTickets = slaWatchlist.stream()
                .filter(CommandCenterResponse.SlaWatchlistItem::isBreached)
                .count();

        if (metrics.getPendingBookings() > 0) {
            alerts.add(CommandCenterResponse.RiskAlert.builder()
                    .severity(metrics.getPendingBookings() > 5 ? "HIGH" : "MEDIUM")
                    .title("Booking queue needs review")
                    .message(metrics.getPendingBookings() + " booking request(s) are waiting for admin approval.")
                    .action("Open the booking queue and approve or reject pending requests.")
                    .build());
        }

        if (metrics.getOutOfServiceResources() > 0) {
            alerts.add(CommandCenterResponse.RiskAlert.builder()
                    .severity("HIGH")
                    .title("Resource availability reduced")
                    .message(metrics.getOutOfServiceResources() + " resource(s) are marked out of service.")
                    .action("Review affected resources and coordinate maintenance updates.")
                    .build());
        }

        if (breachedTickets > 0) {
            alerts.add(CommandCenterResponse.RiskAlert.builder()
                    .severity("HIGH")
                    .title("Ticket SLA watchlist breached")
                    .message(breachedTickets + " active ticket(s) are older than the target response window.")
                    .action("Assign technicians or update ticket status for the oldest incidents.")
                    .build());
        }

        if (alerts.isEmpty()) {
            alerts.add(CommandCenterResponse.RiskAlert.builder()
                    .severity("INFO")
                    .title("Campus operations stable")
                    .message("No urgent booking, resource, or ticket risks are active right now.")
                    .action("Continue monitoring the dashboard for status changes.")
                    .build());
        }

        return alerts;
    }

    private List<CommandCenterResponse.SlaWatchlistItem> buildSlaWatchlist(List<Ticket> tickets) {
        LocalDateTime now = LocalDateTime.now();

        return tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN
                        || ticket.getStatus() == TicketStatus.IN_PROGRESS)
                .map(ticket -> mapSlaWatchlistItem(ticket, now))
                .sorted(Comparator
                        .comparing(CommandCenterResponse.SlaWatchlistItem::isBreached, Comparator.reverseOrder())
                        .thenComparing(item -> priorityWeight(item.getPriority()), Comparator.reverseOrder())
                        .thenComparing(CommandCenterResponse.SlaWatchlistItem::getAgeHours, Comparator.reverseOrder()))
                .limit(8)
                .toList();
    }

    private CommandCenterResponse.SlaWatchlistItem mapSlaWatchlistItem(Ticket ticket, LocalDateTime now) {
        long ageHours = ticket.getCreatedAt() == null
                ? 0
                : Math.max(0, Duration.between(ticket.getCreatedAt(), now).toHours());
        long targetHours = targetHours(ticket.getPriority());

        return CommandCenterResponse.SlaWatchlistItem.builder()
                .ticketId(ticket.getId())
                .title(ticket.getTitle())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : "UNKNOWN")
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : "MEDIUM")
                .assignedTo(ticket.getAssignedTo())
                .ageHours(ageHours)
                .targetHours(targetHours)
                .breached(ageHours > targetHours)
                .build();
    }

    private long targetHours(TicketPriority priority) {
        if (priority == TicketPriority.HIGH) {
            return 8;
        }
        if (priority == TicketPriority.LOW) {
            return 48;
        }
        return 24;
    }

    private int priorityWeight(String priority) {
        if (Objects.equals(priority, TicketPriority.HIGH.name())) {
            return 3;
        }
        if (Objects.equals(priority, TicketPriority.MEDIUM.name())) {
            return 2;
        }
        return 1;
    }

    private long countBookings(List<Booking> bookings, BookingStatus status) {
        return bookings.stream()
                .filter(booking -> booking.getStatus() == status)
                .count();
    }

    private long countTickets(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream()
                .filter(ticket -> ticket.getStatus() == status)
                .count();
    }

    private long countResources(List<Resource> resources, ResourceStatus status) {
        return resources.stream()
                .filter(resource -> resource.getStatus() == status)
                .count();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
