package com.verify_x.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferLetterRequestDTO {

    // Company name
    @NotBlank(message = "Company name is required")
    @Size(
            max = 200,
            message = "Company name cannot exceed 200 characters"
    )
    private String companyName;


    // Job designation
    @Size(
            max = 150,
            message = "Designation cannot exceed 150 characters"
    )
    private String designation;


    // CTC in LPA
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "CTC cannot be negative"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Invalid CTC format"
    )
    private BigDecimal ctc;


    // Monthly stipend during probation
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Monthly stipend cannot be negative"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Invalid monthly stipend format"
    )
    private BigDecimal monthlyStipend;


    // Annual salary after probation
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Annual salary cannot be negative"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Invalid annual salary format"
    )
    private BigDecimal annualSalary;


    // Offer date
    @NotNull(message = "Offer date is required")
    private LocalDate offerDate;


    // Joining date
    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;


    // Offer reference number
    @Size(
            max = 100,
            message = "Reference number cannot exceed 100 characters"
    )
    private String referenceNumber;
}