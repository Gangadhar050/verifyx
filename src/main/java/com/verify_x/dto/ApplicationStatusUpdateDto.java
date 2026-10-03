package com.verify_x.dto;

import com.verify_x.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationStatusUpdateDto {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private String remarks;

    private List<LocalDateTime> interviewSlots;
}
