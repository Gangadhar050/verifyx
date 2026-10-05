package com.verify_x.dto;

import com.verify_x.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Candidate name and email are NOT here on purpose:
 * they are fetched from the Candidate record.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrOfferLetterRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String companyName;

    @NotBlank(message = "Designation is required")
    @Size(max = 150, message = "Designation cannot exceed 150 characters")
    private String designation;

    @NotNull(message = "Offered CTC is required")
    @DecimalMin(value = "0.01", message = "Offered CTC must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid CTC format")
    private BigDecimal offeredCtc;

    @NotNull(message = "Offer date is required")
    private LocalDate offerDate;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotBlank(message = "Reference number is required")
    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String referenceNumber;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;
}