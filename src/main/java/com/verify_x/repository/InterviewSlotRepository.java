package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewSlotRepository
        extends JpaRepository<InterviewSlot, Long> {

    List<InterviewSlot> findByCandidateOrderBySlotDateTimeAsc(
            Candidate candidate);

    Optional<InterviewSlot> findByIdAndCandidate(
            Long id,
            Candidate candidate);

    boolean existsByCandidateAndSlotDateTime(
            Candidate candidate,
            LocalDateTime slotDateTime);
}