package com.verify_x.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateContactDetailsRequest {

    // =========================================================
    // PERMANENT ADDRESS
    // =========================================================

    @NotBlank(message = "Permanent address line 1 is required.")
    @Size(
            max = 500,
            message = "Permanent address line 1 cannot exceed 500 characters."
    )
    private String permanentAddressLine1;

    @Size(
            max = 500,
            message = "Permanent address line 2 cannot exceed 500 characters."
    )
    private String permanentAddressLine2;

    @Size(
            max = 255,
            message = "Area / locality cannot exceed 255 characters."
    )
    private String permanentAreaLocality;

    @NotBlank(message = "Permanent pincode is required.")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Permanent pincode must be a valid 6-digit Indian pincode."
    )
    private String permanentPincode;

    @NotBlank(message = "Permanent city is required.")
    @Size(max = 100)
    private String permanentCity;

    @Size(max = 100)
    private String permanentDistrict;

    @NotBlank(message = "Permanent state is required.")
    @Size(max = 100)
    private String permanentState;

    @NotBlank(message = "Permanent country is required.")
    @Size(max = 100)
    private String permanentCountry;

    /**
     * These are supplied when address is fetched
     * from Google Maps.
     *
     * They can be null for manual address entry.
     */
//    private Double permanentLatitude;
//
//    private Double permanentLongitude;


    // =========================================================
    // EMERGENCY CONTACT
    // =========================================================

    @NotBlank(message = "Emergency contact number is required.")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Emergency contact number must be a valid 10-digit Indian mobile number."
    )
    private String emergencyContactNumber;


    // =========================================================
    // REFERRAL CONTACT - OPTIONAL
    // =========================================================

    @Pattern(
            regexp = "^$|^[6-9][0-9]{9}$",
            message = "Referral contact number must be a valid 10-digit Indian mobile number."
    )
    private String referralContactNumber;
}