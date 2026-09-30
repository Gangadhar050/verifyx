package com.verify_x.serviceImpl;

import com.verify_x.dto.InterviewCandidateDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Interview;
import com.verify_x.entity.InterviewSlot;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.InterviewSlotStatus;
import com.verify_x.enums.InterviewStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.InterviewRepository;
import com.verify_x.repository.InterviewSlotRepository;
import com.verify_x.services.InterviewService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewSlotRepository interviewSlotRepository;

    @Override
    public List<InterviewCandidateDto> getInterviewCandidates() {

        return candidateRepository.findAll()
                .stream()
                .filter(candidate ->
                        candidate.getApplicationStatus()
                                == ApplicationStatus.APPROVED)
                .map(this::mapToDto)
                .toList();
    }

    private InterviewCandidateDto mapToDto(
            Candidate candidate) {

        Interview interview =
                interviewRepository
                        .findByCandidate(candidate)
                        .orElse(null);

        InterviewSlot selectedSlot = null;

        if (interview != null &&
                interview.getSelectedSlot() != null) {

            selectedSlot = interview.getSelectedSlot();

        } else {

            selectedSlot =
                    interviewSlotRepository
                            .findByCandidateOrderBySlotDateTimeAsc(
                                    candidate)
                            .stream()
                            .filter(slot ->
                                    slot.getStatus()
                                            == InterviewSlotStatus.SELECTED)
                            .findFirst()
                            .orElse(null);
        }

        return InterviewCandidateDto.builder()
                .candidateId(candidate.getId())
                .candidateName(candidate.getUsername())
                .email(candidate.getEmail())
                .appliedRole(
                        candidate.getAppliedRole() != null
                                ? candidate.getAppliedRole().name()
                                : null)
                .bgvStatus(
                        candidate.getApplicationStatus() != null
                                ? candidate.getApplicationStatus().name()
                                : null)
                .selectedSlotId(
                        selectedSlot != null
                                ? selectedSlot.getId()
                                : null)
                .selectedInterviewSlot(
                        selectedSlot != null
                                ? selectedSlot.getSlotDateTime()
                                : null)
                .interviewStatus(
                        interview != null
                                ? interview.getInterviewStatus()
                                : InterviewStatus.AWAITING_SLOT)
                .build();
    }

    @Override
    public void confirmInterview(
            Long candidateId,
            String confirmedBy) {

        Interview interview = getInterview(candidateId);

        if (interview.getSelectedSlot() == null) {
            throw new BadRequestException(
                    "Candidate has not selected an interview slot.");
        }

        interview.setInterviewStatus(
                InterviewStatus.CONFIRMED);

        interview.setConfirmedBy(confirmedBy);

        interview.setConfirmedAt(
                LocalDateTime.now());

        interviewRepository.save(interview);
    }

    @Override
    public void approveInterview(
            Long candidateId,
            String remarks) {

        Interview interview = getInterview(candidateId);

        if (interview.getInterviewStatus()
                != InterviewStatus.CONFIRMED) {

            throw new BadRequestException(
                    "Interview must be confirmed first.");
        }

        interview.setInterviewStatus(
                InterviewStatus.APPROVED);

        interview.setInterviewRemarks(remarks);

        interviewRepository.save(interview);
    }

    @Override
    public void rejectInterview(
            Long candidateId,
            String remarks) {

        Interview interview = getInterview(candidateId);

        interview.setInterviewStatus(
                InterviewStatus.REJECTED);

        interview.setInterviewRemarks(remarks);

        interviewRepository.save(interview);
    }

    private Interview getInterview(Long candidateId) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));

        return interviewRepository
                .findByCandidate(candidate)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Interview record not found."));
    }
}