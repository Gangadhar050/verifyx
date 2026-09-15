package com.verify_x.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentAddressRequest {

    @NotNull(message = "Current latitude is required.")
    @DecimalMin(value = "-90.0", message = "Invalid latitude.")
    @DecimalMax(value = "90.0", message = "Invalid latitude.")
    private Double latitude;

    @NotNull(message = "Current longitude is required.")
    @DecimalMin(value = "-180.0", message = "Invalid longitude.")
    @DecimalMax(value = "180.0", message = "Invalid longitude.")
    private Double longitude;

    @AssertTrue(message = "You must confirm the live photo and location are accurate.")
    private boolean locationConfirmed;

    @AssertTrue(message = "You must confirm the photo clearly shows the front/entrance of your house.")
    private boolean photoConfirmed;
}