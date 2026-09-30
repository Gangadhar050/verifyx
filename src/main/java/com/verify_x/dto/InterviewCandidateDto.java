package com.verify_x.dto;

import com.verify_x.enums.InterviewStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewCandidateDto {

    private Long candidateId;

    private String candidateName;

    private String email;

    private String appliedRole;

    private String bgvStatus;

    private Long selectedSlotId;

    private LocalDateTime selectedInterviewSlot;

    private InterviewStatus interviewStatus;
}