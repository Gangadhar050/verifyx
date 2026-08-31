package com.verify_x.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verify_x.exception.GeocodingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;

/**
 * Thin wrapper around Google's Geocoding API for reverse
 * geocoding (lat/lng -> human-readable address).
 *
 * Docs: https://developers.google.com/maps/documentation/geocoding/requests-reverse-geocoding
 *
 * Requires a valid Google Maps Geocoding API key with
 * "Geocoding API" enabled on the Google Cloud project,
 * configured via:
 *
 *   google.maps.api-key=${GOOGLE_MAPS_API_KEY:}
 */
@Component
@Slf4j
public class GoogleGeocodingClient {

    private static final String GEOCODE_URL =
            "https://maps.googleapis.com/maps/api/geocode/json";

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(8);

    @Value("${google.maps.api-key:}")
    private String apiKey;

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(REQUEST_TIMEOUT)
                    .build();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    /**
     * Reverse-geocodes the given coordinates into a
     * structured address.
     *
     * @throws GeocodingException if the API key is missing,
     *         the call fails, or Google returns no results.
     */
    public GeocodeResult reverseGeocode(
            Double latitude,
            Double longitude) {

        if (apiKey == null || apiKey.isBlank()) {

            throw new GeocodingException(
                    "Google Maps API key is not configured. " +
                            "Set GOOGLE_MAPS_API_KEY."
            );
        }

        if (latitude == null || longitude == null) {

            throw new GeocodingException(
                    "Latitude and longitude are required for geocoding."
            );
        }

        String latLng =
                latitude + "," + longitude;

        String url =
                GEOCODE_URL +
                        "?latlng=" + URLEncoder.encode(
                        latLng,
                        StandardCharsets.UTF_8
                ) +
                        "&key=" + URLEncoder.encode(
                        apiKey,
                        StandardCharsets.UTF_8
                );

        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

        HttpResponse<String> httpResponse;

        try {

            httpResponse =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

        } catch (IOException | InterruptedException exception) {

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            log.error(
                    "Geocoding request failed. lat={}, lng={}",
                    latitude,
                    longitude,
                    exception
            );

            throw new GeocodingException(
                    "Unable to reach the geocoding service. Please try again.",
                    exception
            );
        }

        if (httpResponse.statusCode() != 200) {

            log.error(
                    "Geocoding API returned HTTP {}. body={}",
                    httpResponse.statusCode(),
                    httpResponse.body()
            );

            throw new GeocodingException(
                    "Geocoding service returned an unexpected error."
            );
        }

        return parseResponse(httpResponse.body());
    }

    // =========================================================
    // PARSE GOOGLE RESPONSE
    // =========================================================

    private GeocodeResult parseResponse(String responseBody) {

        JsonNode root;

        try {

            root = objectMapper.readTree(responseBody);

        } catch (IOException exception) {

            log.error(
                    "Failed to parse geocoding response: {}",
                    responseBody,
                    exception
            );

            throw new GeocodingException(
                    "Unable to process geocoding response.",
                    exception
            );
        }

        String status =
                root.path("status").asText("");

        if (!"OK".equals(status)) {

            String errorMessage =
                    root.path("error_message")
                            .asText("No results found for the given location.");

            log.warn(
                    "Geocoding API status={}, message={}",
                    status,
                    errorMessage
            );

            throw new GeocodingException(
                    "Unable to resolve address for the given location: " +
                            errorMessage
            );
        }

        JsonNode results =
                root.path("results");

        if (!results.isArray() || results.isEmpty()) {

            throw new GeocodingException(
                    "No address found for the given coordinates."
            );
        }

        JsonNode firstResult =
                results.get(0);

        String formattedAddress =
                firstResult.path("formatted_address")
                        .asText(null);

        JsonNode addressComponents =
                firstResult.path("address_components");

        String pincode = null;
        String city = null;
        String district = null;
        String state = null;
        String country = null;

        if (addressComponents.isArray()) {

            Iterator<JsonNode> iterator =
                    addressComponents.elements();

            while (iterator.hasNext()) {

                JsonNode component = iterator.next();

                String longName =
                        component.path("long_name").asText(null);

                JsonNode types =
                        component.path("types");

                if (!types.isArray()) {
                    continue;
                }

                for (JsonNode typeNode : types) {

                    String type =
                            typeNode.asText("");

                    switch (type) {

                        case "postal_code":
                            pincode = longName;
                            break;

                        case "locality":
                            if (city == null) {
                                city = longName;
                            }
                            break;

                        case "sublocality":
                        case "sublocality_level_1":
                            if (city == null) {
                                city = longName;
                            }
                            break;

                        case "administrative_area_level_2":
                            district = longName;
                            break;

                        case "administrative_area_level_1":
                            state = longName;
                            break;

                        case "country":
                            country = longName;
                            break;

                        default:
                            break;
                    }
                }
            }
        }

        if (formattedAddress == null || formattedAddress.isBlank()) {

            throw new GeocodingException(
                    "Geocoding service did not return a valid address."
            );
        }

        return new GeocodeResult(
                formattedAddress,
                pincode,
                city,
                district,
                state,
                country);
    }

    // =========================================================
    // RESULT
    // =========================================================

    public record GeocodeResult(
            String address,
            String pincode,
            String city,
            String district,
            String state,
            String country
    ) {
    }
}