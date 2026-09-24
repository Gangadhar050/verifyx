package com.verify_x.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verify_x.exception.GeocodingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/**
 * Reverse geocoding client backed by OpenStreetMap's free Nominatim API.
 *
 * Docs: https://nominatim.org/release-docs/latest/api/Reverse/
 *
 * No API key, no billing account, and no credit card are required - this
 * is a genuinely free public service. It does have a strict usage policy:
 *   - Max ~1 request/second per client (fine for this app's usage pattern
 *     of "one candidate captures their address once").
 *   - A descriptive User-Agent header identifying the application is
 *     REQUIRED, or Nominatim will reject/block requests. Set it below to
 *     something identifying your actual app + a contact email/URL, per
 *     https://operations.osmfoundation.org/policies/nominatim/
 *   - Do not send excessive automated/bulk traffic. This client is meant
 *     for individual, user-triggered lookups, not batch processing.
 *
 * The class name, method signature, and GeocodeResult record are kept
 * identical to the previous Google-based client so no other file in the
 * codebase (service, DTOs, controller) needs to change.
 */
@Component
@Slf4j
public class GoogleGeocodingClient {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/reverse";

    // REQUIRED by Nominatim's usage policy - identify your app here.
    // Replace the email with a real contact address for this project.
    private static final String USER_AGENT =
            "VerifyX-Backend/1.0 (contact: admin@verifyx.com)";

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(REQUEST_TIMEOUT)
                    .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeocodeResult reverseGeocode(Double latitude, Double longitude) {

        if (latitude == null || longitude == null) {
            throw new GeocodingException(
                    "Latitude and longitude are required for geocoding."
            );
        }

        String url = String.format(
                Locale.ROOT,
                "%s?format=jsonv2&lat=%s&lon=%s&addressdetails=1&zoom=18",
                NOMINATIM_URL,
                latitude,
                longitude
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> httpResponse;

        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Geocoding request failed. lat={}, lng={}", latitude, longitude, exception);
            throw new GeocodingException(
                    "Unable to reach the geocoding service. Please try again.", exception
            );
        }

        if (httpResponse.statusCode() != 200) {
            log.error("Nominatim API returned HTTP {}. body={}",
                    httpResponse.statusCode(), httpResponse.body());
            throw new GeocodingException("Geocoding service returned an unexpected error.");
        }

        return parseResponse(httpResponse.body());
    }

    private GeocodeResult parseResponse(String responseBody) {

        JsonNode root;

        try {
            root = objectMapper.readTree(responseBody);
        } catch (IOException exception) {
            log.error("Failed to parse geocoding response: {}", responseBody, exception);
            throw new GeocodingException("Unable to process geocoding response.", exception);
        }

        if (root.has("error")) {
            String errorMessage = root.path("error").asText("No results found for the given location.");
            log.warn("Nominatim returned error: {}", errorMessage);
            throw new GeocodingException(
                    "Unable to resolve address for the given location: " + errorMessage
            );
        }

        String formattedAddress = root.path("display_name").asText(null);

        if (formattedAddress == null || formattedAddress.isBlank()) {
            throw new GeocodingException("Geocoding service did not return a valid address.");
        }

        JsonNode addr = root.path("address");

        // Nominatim's address breakdown uses different key names than Google's.
        // house_number + road give the street-level line; the various
        // neighbourhood/suburb fields map to "area/locality"; city has
        // several possible fallback keys depending on how OSM tagged the area.
        String houseNumber = textOrNull(addr, "house_number");
        String road = textOrNull(addr, "road");

        String areaLocality = firstNonBlank(
                textOrNull(addr, "neighbourhood"),
                textOrNull(addr, "suburb"),
                textOrNull(addr, "quarter"),
                textOrNull(addr, "residential")
        );

        String city = firstNonBlank(
                textOrNull(addr, "city"),
                textOrNull(addr, "town"),
                textOrNull(addr, "village"),
                textOrNull(addr, "municipality"),
                areaLocality
        );

        String district = firstNonBlank(
                textOrNull(addr, "state_district"),
                textOrNull(addr, "county")
        );

        String state = textOrNull(addr, "state");

        String country = textOrNull(addr, "country");

        String pincode = textOrNull(addr, "postcode");

        String addressLine1 = buildAddressLine1(houseNumber, road, areaLocality, formattedAddress);

        return new GeocodeResult(
                formattedAddress,
                addressLine1,
                areaLocality,
                pincode,
                city,
                district,
                state,
                country
        );
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText(null);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String buildAddressLine1(String houseNumber, String road, String areaLocality, String formattedAddress) {

        StringBuilder line1 = new StringBuilder();

        if (houseNumber != null && !houseNumber.isBlank()) {
            line1.append(houseNumber).append(" ");
        }
        if (road != null && !road.isBlank()) {
            line1.append(road);
        }

        String result = line1.toString().trim();

        if (!result.isEmpty()) {
            return result;
        }

        if (areaLocality != null && !areaLocality.isBlank()) {
            return areaLocality;
        }

        // Last resort: take the first comma-separated segment of the full address
        String[] parts = formattedAddress.split(",");
        return parts.length > 0 ? parts[0].trim() : formattedAddress;
    }

    public record GeocodeResult(
            String address,
            String addressLine1,
            String areaLocality,
            String pincode,
            String city,
            String district,
            String state,
            String country
    ) {
    }
}