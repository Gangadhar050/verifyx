package com.verify_x.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermanentAddressResponse {

    private Long id;
    private Long candidateId;

    private String permanentAddressLine1;
    private String permanentAddressLine2;
    private String permanentAreaLocality;
    private String permanentPincode;
    private String permanentCity;
    private String permanentDistrict;
    private String permanentState;
    private String permanentCountry;

    private String emergencyContactNumber;
    private String referralContactNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}