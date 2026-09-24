package com.verify_x.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentAddressRequest {

    @NotBlank(message = "Current address line 1 is required.")
    private String currentAddressLine1;

    private String currentAddressLine2;

    private String currentAreaLocality;

    @NotBlank(message = "Current pincode is required.")
    private String currentPincode;

    @NotBlank(message = "Current city is required.")
    private String currentCity;

    private String currentDistrict;

    @NotBlank(message = "Current state is required.")
    private String currentState;

    @NotBlank(message = "Current country is required.")
    private String currentCountry;
private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
//    @DecimalMin(value = "-90.0", message = "Invalid latitude.")
//    @DecimalMax(value = "90.0", message = "Invalid latitude.")
//    private Double currentLatitude;
//
//    @DecimalMin(value = "-180.0", message = "Invalid longitude.")
//    @DecimalMax(value = "180.0", message = "Invalid longitude.")
//    private Double currentLongitude;
}