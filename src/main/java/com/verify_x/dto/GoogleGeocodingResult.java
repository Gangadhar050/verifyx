package com.verify_x.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleGeocodingResult {

    private String formattedAddress;

    private String placeId;
}