package com.verify_x.dto;

import com.verify_x.enums.EmploymentType;
import com.verify_x.enums.OfferLetterMailStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrOfferLetterResponseDTO {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String companyName;
    private String designation;
    private BigDecimal offeredCtc;
    private LocalDate offerDate;
    private LocalDate joiningDate;
    private String referenceNumber;
    private EmploymentType employmentType;
    private OfferLetterMailStatus mailStatus;
    private String sentBy;
    private LocalDateTime createdAt;
}