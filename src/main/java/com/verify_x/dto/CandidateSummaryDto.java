package com.verify_x.dto;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.AppliedRole;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.TechnicalSkill;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CandidateSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private CandidateType candidateType;
    private AppliedRole appliedRole;
    private List<TechnicalSkill> skills;
    private String panNumber;
    private String aadhaarNumber;
    private String uanNumber;
    private String uanVerificationStatus;
    private boolean uanVerified;
    private ApplicationStatus applicationStatus;
}