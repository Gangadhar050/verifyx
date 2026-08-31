//package com.verify_x.dto;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;
//import jakarta.validation.constraints.Size;
//
//import lombok.*;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CandidateContactDetailsRequest1 {
//
//    /**
//     * Permanent address.
//     *
//     * Candidate enters this manually.
//     */
//    @NotBlank(message = "Permanent address is required.")
//    @Size(
//            min = 5,
//            max = 1000,
//            message = "Permanent address must be between 5 and 1000 characters."
//    )
//    private String permanentAddress;
//
//    /**
//     * Current / residential address.
//     *
//     * Candidate enters this manually.
//     */
//    @NotBlank(message = "Residential address is required.")
//    @Size(
//            min = 5,
//            max = 1000,
//            message = "Residential address must be between 5 and 1000 characters."
//    )
//    private String residentialAddress;
//
//    /**
//     * Mandatory emergency contact.
//     */
//    @NotBlank(message = "Emergency contact number is required.")
//    @Pattern(
//            regexp = "^[6-9][0-9]{9}$",
//            message = "Emergency contact number must be a valid 10-digit Indian mobile number."
//    )
//    private String emergencyContactNumber;
//
//    /**
//     * Optional referral contact.
//     *
//     * Empty or null is allowed.
//     */
//    @Pattern(
//            regexp = "^$|^[6-9][0-9]{9}$",
//            message = "Referral contact number must be a valid 10-digit Indian mobile number."
//    )
//    private String referralContactNumber;
//}