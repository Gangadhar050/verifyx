package com.verify_x.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentAddressResponse {

    private Long id;
    private Long candidateId;

    private String currentAddress;
    private String currentAddressLine1;
    private String currentAddressLine2;
    private String currentAreaLocality;
    private String currentPincode;
    private String currentCity;
    private String currentDistrict;
    private String currentState;
    private String currentCountry;
//    private Double currentLatitude;
//    private Double currentLongitude;
private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}