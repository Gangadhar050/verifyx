package com.verify_x.dto;

import com.verify_x.enums.InterviewSlotStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlotResponseDto {

    private Long id;
    private LocalDateTime slotDateTime;
    private InterviewSlotStatus status;
}