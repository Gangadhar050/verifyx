package com.verify_x.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlotRequestDto {

    @NotEmpty(message = "At least one interview slot is required.")
    private List<LocalDateTime> slots;
}