package com.verify_x.serviceImpl;

import com.verify_x.dto.InterviewSlotResponseDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Interview;
import com.verify_x.entity.InterviewSlot;
import com.verify_x.enums.InterviewSlotStatus;
import com.verify_x.enums.InterviewStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.InterviewRepository;
import com.verify_x.repository.InterviewSlotRepository;
import com.verify_x.services.EmailService;
import com.verify_x.services.InterviewSlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewSlotServiceImpl
        implements InterviewSlotService {

    private final CandidateRepository candidateRepository;

    private final InterviewSlotRepository interviewSlotRepository;

    private final InterviewRepository interviewRepository;

    private final EmailService emailService;


    // ============================================================
    // HR: CREATE GLOBAL INTERVIEW SLOTS
    // ============================================================

    @Override
    public void createInterviewSlots( Long candidateId, List<LocalDateTime> slots) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate",
                                candidateId
                        )
                );

        if (slots == null || slots.isEmpty()) {
            throw new BadRequestException(
                    "At least one interview slot is required."
            );
        }

        /*
         * Remove duplicates from request.
         */
        Set<LocalDateTime> uniqueSlots =
                new HashSet<>(slots);

        if (uniqueSlots.size() != slots.size()) {
            throw new BadRequestException(
                    "Duplicate interview slots are not allowed."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        for (LocalDateTime slot : slots) {

            if (slot == null) {
                throw new BadRequestException(
                        "Interview slot cannot be null."
                );
            }

            if (!slot.isAfter(now)) {
                throw new BadRequestException(
                        "Interview slot must be in the future: "
                                + slot
                );
            }

            /*
             * Do not create the same global slot twice.
             */
            if (interviewSlotRepository
                    .existsBySlotDateTime(slot)) {

                throw new BadRequestException(
                        "Interview slot already exists: "
                                + slot
                );
            }
        }

        List<InterviewSlot> interviewSlots =
                slots.stream()
                        .map(slot ->
                                InterviewSlot.builder()

                                        // Saves candidate ID in interview_slots.candidate_id
                                        .candidate(candidate)

                                        // Saves selected HR-provided interview time
                                        .slotDateTime(slot)

                                        // Candidate has not selected it yet
                                        .status(
                                                InterviewSlotStatus.AVAILABLE
                                        )

                                        .build()
                        )
                        .toList();

        interviewSlotRepository.saveAll(interviewSlots);

        log.info(
                "Created {} global interview slots.",
                interviewSlots.size()
        );
    }


    // ============================================================
    // CANDIDATE: GET AVAILABLE GLOBAL SLOTS
    // ============================================================

    @Override
    @Transactional
    public List<InterviewSlotResponseDto>
    getAvailableSlots() {

        return interviewSlotRepository
                .findByStatusAndSlotDateTimeAfterOrderBySlotDateTimeAsc(
                        InterviewSlotStatus.AVAILABLE,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ============================================================
    // CANDIDATE: SELECT SLOT
    // ============================================================
    @Override
    public void selectInterviewSlot(
            Long candidateId,
            Long slotId) {

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId
                                )
                        );

        /*
         * Gets only a slot belonging to the logged-in candidate.
         * This verifies candidate_id mapping.
         */
        InterviewSlot selectedSlot =
                interviewSlotRepository
                        .findByIdAndCandidate(
                                slotId,
                                candidate
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Interview slot is not assigned to you "+ candidateId
                                )
                        );

        if (selectedSlot.getStatus()
                != InterviewSlotStatus.AVAILABLE) {

            throw new BadRequestException(
                    "This interview slot is no longer available."
            );
        }

        /*
         * Get all slots assigned to this candidate.
         */
        List<InterviewSlot> slots =
                interviewSlotRepository
                        .findByCandidateOrderBySlotDateTimeAsc(
                                candidate
                        );

        /*
         * Selected slot becomes SELECTED.
         * All other candidate slots become CANCELLED.
         */
        for (InterviewSlot slot : slots) {

            if (slot.getId().equals(slotId)) {

                slot.setStatus(
                        InterviewSlotStatus.SELECTED
                );

                slot.setSelectedAt(
                        LocalDateTime.now()
                );

            } else {

                slot.setStatus(
                        InterviewSlotStatus.CANCELLED
                );
            }
        }

        interviewSlotRepository.saveAll(slots);

        /*
         * Create or update the Interview record.
         */
        Interview interview =
                interviewRepository
                        .findByCandidate(candidate)
                        .orElse(
                                Interview.builder()
                                        .candidate(candidate)
                                        .build()
                        );

        interview.setSelectedSlot(selectedSlot);

        interview.setInterviewStatus(
                InterviewStatus.SLOT_SELECTED
        );

        interviewRepository.save(interview);

        emailService.sendInterviewSlotSelectedEmailToHr(
                candidate.getUsername(),
                candidate.getEmail(),
                selectedSlot.getSlotDateTime()
        );
    }


    // ============================================================
    // HR: GET ALL SLOTS
    // ============================================================

    @Override
    @Transactional
    public List<InterviewSlotResponseDto>
    getAllSlots() {

        return interviewSlotRepository
                .findAllByOrderBySlotDateTimeAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ============================================================
    // RESPONSE MAPPER
    // ============================================================

    private InterviewSlotResponseDto mapToResponse(
            InterviewSlot slot) {

        return InterviewSlotResponseDto.builder()
                .id(slot.getId())
                .slotDateTime(
                        slot.getSlotDateTime()
                )
                .status(
                        slot.getStatus()
                )
                .build();
    }
}