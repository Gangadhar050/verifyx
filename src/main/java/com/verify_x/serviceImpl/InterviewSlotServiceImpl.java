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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewSlotServiceImpl
        implements InterviewSlotService {

    private final CandidateRepository candidateRepository;
    private final InterviewSlotRepository interviewSlotRepository;
    private final EmailService emailService;
    private final InterviewRepository interviewRepository;


    // ============================================================
    // HR: CREATE AND SEND INTERVIEW SLOTS
    // ============================================================

    @Override
    public void createAndSendInterviewSlots(
            Long candidateId,
            List<LocalDateTime> slots) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));

        // --------------------------------------------------------
        // Validate slots
        // --------------------------------------------------------

        if (slots == null || slots.isEmpty()) {

            throw new BadRequestException(
                    "At least one interview slot is required.");
        }

        // Remove null values / validate
        for (LocalDateTime slot : slots) {

            if (slot == null) {

                throw new BadRequestException(
                        "Interview slot cannot be null.");
            }

            if (!slot.isAfter(LocalDateTime.now())) {

                throw new BadRequestException(
                        "Interview slot must be in the future.");
            }
        }

        // --------------------------------------------------------
        // Prevent duplicate slots in same request
        // --------------------------------------------------------

        Set<LocalDateTime> uniqueSlots =
                new HashSet<>(slots);

        if (uniqueSlots.size() != slots.size()) {

            throw new BadRequestException(
                    "Duplicate interview slots are not allowed.");
        }

        // --------------------------------------------------------
        // Prevent duplicate slots already stored for candidate
        // --------------------------------------------------------

        for (LocalDateTime slot : slots) {

            if (interviewSlotRepository
                    .existsByCandidateAndSlotDateTime(
                            candidate,
                            slot)) {

                throw new BadRequestException(
                        "Interview slot already exists: " + slot);
            }
        }

        // --------------------------------------------------------
        // Create InterviewSlot records
        // --------------------------------------------------------

        List<InterviewSlot> interviewSlots =
                slots.stream()
                        .map(slot ->
                                InterviewSlot.builder()
                                        .candidate(candidate)
                                        .slotDateTime(slot)
                                        .status(
                                                InterviewSlotStatus.AVAILABLE)
                                        .build())
                        .toList();

        interviewSlotRepository.saveAll(interviewSlots);

        // --------------------------------------------------------
        // Send email to candidate
        // --------------------------------------------------------

        emailService.sendInterviewSlotsEmail(
                candidate.getEmail(),
                candidate.getUsername(),
                slots
        );

        /*
         * IMPORTANT:
         *
         * Do NOT create/update Interview here.
         *
         * Candidate has not selected a slot yet.
         */
    }


    // ============================================================
    // CANDIDATE: GET AVAILABLE INTERVIEW SLOTS
    // ============================================================

    @Override
    public List<InterviewSlotResponseDto> getCandidateSlots(
            Long candidateId) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));

        return interviewSlotRepository
                .findByCandidateOrderBySlotDateTimeAsc(candidate)
                .stream()
                .map(slot ->
                        InterviewSlotResponseDto.builder()
                                .id(slot.getId())
                                .slotDateTime(
                                        slot.getSlotDateTime())
                                .status(
                                        slot.getStatus())
                                .build())
                .toList();
    }


    // ============================================================
    // CANDIDATE: SELECT INTERVIEW SLOT
    // ============================================================

    @Override
    public void selectInterviewSlot(
            Long candidateId,
            Long slotId) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));

        // --------------------------------------------------------
        // Find selected slot
        // --------------------------------------------------------

        InterviewSlot selectedSlot =
                interviewSlotRepository
                        .findByIdAndCandidate(
                                slotId,
                                candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Interview slot not found."));

        // --------------------------------------------------------
        // Check whether slot is available
        // --------------------------------------------------------

        if (selectedSlot.getStatus()
                != InterviewSlotStatus.AVAILABLE) {

            throw new BadRequestException(
                    "This interview slot is no longer available.");
        }

        // --------------------------------------------------------
        // Get all slots for candidate
        // --------------------------------------------------------

        List<InterviewSlot> slots =
                interviewSlotRepository
                        .findByCandidateOrderBySlotDateTimeAsc(
                                candidate);

        // --------------------------------------------------------
        // Select one slot
        // Cancel remaining slots
        // --------------------------------------------------------

        for (InterviewSlot slot : slots) {

            if (slot.getId().equals(slotId)) {

                slot.setStatus(
                        InterviewSlotStatus.SELECTED);

                slot.setSelectedAt(
                        LocalDateTime.now());

            } else {

                slot.setStatus(
                        InterviewSlotStatus.CANCELLED);
            }
        }

        interviewSlotRepository.saveAll(slots);


        // ========================================================
        // CREATE / UPDATE INTERVIEW RECORD
        // ========================================================

        Interview interview =
                interviewRepository
                        .findByCandidate(candidate)
                        .orElse(
                                Interview.builder()
                                        .candidate(candidate)
                                        .build()
                        );

        // Store selected slot
        interview.setSelectedSlot(
                selectedSlot);

        // Move interview pipeline to SLOT_SELECTED
        interview.setInterviewStatus(
                InterviewStatus.SLOT_SELECTED);

        interviewRepository.save(interview);
    }
}