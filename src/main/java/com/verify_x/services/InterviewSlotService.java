package com.verify_x.services;

import com.verify_x.dto.InterviewSlotResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewSlotService {

    void createAndSendInterviewSlots(
            Long candidateId,
            List<LocalDateTime> slots);

    List<InterviewSlotResponseDto> getCandidateSlots(
            Long candidateId);

    void selectInterviewSlot(
            Long candidateId,
            Long slotId);
}