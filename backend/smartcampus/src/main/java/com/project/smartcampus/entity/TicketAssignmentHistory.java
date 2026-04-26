package com.project.smartcampus.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ticket_assignment_history")
public class TicketAssignmentHistory {

    @Id
    private Long id;

    private Long ticketId;

    private Long assignedBy;

    private Long fromTechnicianId;

    private Long toTechnicianId;

    private String reason;

    private LocalDateTime assignedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Long getFromTechnicianId() {
        return fromTechnicianId;
    }

    public void setFromTechnicianId(Long fromTechnicianId) {
        this.fromTechnicianId = fromTechnicianId;
    }

    public Long getToTechnicianId() {
        return toTechnicianId;
    }

    public void setToTechnicianId(Long toTechnicianId) {
        this.toTechnicianId = toTechnicianId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
