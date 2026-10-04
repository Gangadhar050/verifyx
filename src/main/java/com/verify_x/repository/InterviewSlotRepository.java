package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.InterviewSlot;
import com.verify_x.enums.InterviewSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSlotRepository
        extends JpaRepository<InterviewSlot, Long> {

    /*
     * Gets slots assigned to one candidate.
     */
    List<InterviewSlot> findByCandidateOrderBySlotDateTimeAsc(
            Candidate candidate
    );

    /*
     * Gets one slot only if it belongs to the logged-in candidate.
     */
    Optional<InterviewSlot> findByIdAndCandidate(
            Long id,
            Candidate candidate
    );

    /*
     * Prevents the same candidate from receiving
     * the same time slot twice.
     */
    boolean existsByCandidateAndSlotDateTime(
            Candidate candidate,
            LocalDateTime slotDateTime
    );

    /*
     * Gets currently available future slots.
     */
    List<InterviewSlot>
    findByStatusAndSlotDateTimeAfterOrderBySlotDateTimeAsc(
            InterviewSlotStatus status,
            LocalDateTime dateTime
    );

    /*
     * HR gets all interview slots.
     */
    List<InterviewSlot> findAllByOrderBySlotDateTimeAsc();

    /*
     * Prevents duplicate times globally.
     */
    boolean existsBySlotDateTime(
            LocalDateTime slotDateTime
    );
}