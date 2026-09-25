package com.verify_x.dto;

import com.verify_x.enums.EmploymentStatus;
import com.verify_x.enums.NoticePeriodStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentDetailsDto {

    /*
     * Employment Status
     */

    @NotNull(message = "Employment status is required.")
    private EmploymentStatus employmentStatus;

    /*
     * ===========================
     * CURRENT EMPLOYMENT
     * ===========================
     */

    @Size(max = 100)
    private String currentCompany;

    @Size(max = 100)
    private String currentDesignation;

    @Positive(message = "Current CTC must be greater than zero.")
    private Double currentCTC;

    private LocalDate workingFrom;

    /*
     * ===========================
     * NOTICE PERIOD
     * ===========================
     */

    private NoticePeriodStatus noticePeriodStatus;

    @Positive(message = "Official notice period must be greater than zero.")
    private Integer officialNoticePeriod;

    private LocalDate lastWorkingDate;

    /*
     * ===========================
     * PREVIOUS EMPLOYMENT
     * ===========================
     */

    @Size(max = 100)
    private String previousCompanyName;

    @Size(max = 100)
    private String previousDesignation;

    private LocalDate previousWorkingFrom;

    private LocalDate previousWorkingTo;

    @Positive(message = "Previous CTC must be greater than zero.")
    private Double previousCTC;

    @Positive(message = "Expected CTC must be greater than zero.")
    private Double expectedCTC;

    @Positive(message = "Experience must be greater than zero.")
    private Double totalExperience;

    /*
     * ===========================
     * UAN
     * ===========================
     */

    @Pattern(
            regexp = "^\\d{12}$",
            message = "UAN must contain exactly 12 digits."
    )
    private String uanNumber;

}