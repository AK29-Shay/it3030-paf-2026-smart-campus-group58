package com.project.smartcampus.repository;

import com.project.smartcampus.entity.TicketComment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketCommentRepository extends MongoRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);

    List<TicketComment> findByTicketIdInOrderByCreatedAtAscIdAsc(List<Long> ticketIds);
}