package com.verify_x.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferLetterResponseDTO {

    private Long id;

    private Long candidateId;

    private String companyName;

    private String designation;

    private BigDecimal ctc;

    // Monthly stipend during probation
    private BigDecimal monthlyStipend;

    // Annual salary after probation
    private BigDecimal annualSalary;

    private LocalDate offerDate;

    private LocalDate joiningDate;

    private String referenceNumber;

    private String documentFileName;

    private String documentContentType;

    private Long documentSize;

    private boolean documentAvailable;

    // Offer release status
    private boolean released;

    // Date and time when offer was released
    private LocalDateTime releasedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}