package com.project.smartcampus.repository;

import com.project.smartcampus.entity.TicketAssignmentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketAssignmentHistoryRepository extends MongoRepository<TicketAssignmentHistory, Long> {

    List<TicketAssignmentHistory> findByTicketIdOrderByAssignedAtDescIdDesc(Long ticketId);
}
