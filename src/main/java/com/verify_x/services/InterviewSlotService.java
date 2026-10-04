package com.verify_x.services;

import com.verify_x.dto.InterviewSlotResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewSlotService {

    void createInterviewSlots(Long candidateId, List<LocalDateTime> slots);

    List<InterviewSlotResponseDto> getAvailableSlots();

    void selectInterviewSlot(Long candidateId, Long slotId);

    List<InterviewSlotResponseDto> getAllSlots();
}