package com.verify_x.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlotRequestDto {

    @NotNull(message = "Interview slot date and time is required.")
    @Future(message = "Interview slot must be in the future.")
    private LocalDateTime slotDateTime;
}