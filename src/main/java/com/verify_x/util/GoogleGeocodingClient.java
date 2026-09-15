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

@Component
@Slf4j
public class GoogleGeocodingClient {

    private static final String GEOCODE_URL =
            "https://maps.googleapis.com/maps/api/geocode/json";

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    @Value("${google.maps.api-key:}")
    private String apiKey;

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(REQUEST_TIMEOUT)
                    .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeocodeResult reverseGeocode(Double latitude, Double longitude) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new GeocodingException(
                    "Google Maps API key is not configured. Set GOOGLE_MAPS_API_KEY."
            );
        }

        if (latitude == null || longitude == null) {
            throw new GeocodingException(
                    "Latitude and longitude are required for geocoding."
            );
        }

        String latLng = latitude + "," + longitude;

        String url = GEOCODE_URL
                + "?latlng=" + URLEncoder.encode(latLng, StandardCharsets.UTF_8)
                + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
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
            log.error("Geocoding API returned HTTP {}. body={}",
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

        String status = root.path("status").asText("");

        if (!"OK".equals(status)) {
            String errorMessage = root.path("error_message")
                    .asText("No results found for the given location.");
            log.warn("Geocoding API status={}, message={}", status, errorMessage);
            throw new GeocodingException(
                    "Unable to resolve address for the given location: " + errorMessage
            );
        }

        JsonNode results = root.path("results");

        if (!results.isArray() || results.isEmpty()) {
            throw new GeocodingException("No address found for the given coordinates.");
        }

        JsonNode firstResult = results.get(0);

        String formattedAddress = firstResult.path("formatted_address").asText(null);

        JsonNode addressComponents = firstResult.path("address_components");

        String streetNumber = null;
        String route = null;
        String areaLocality = null;
        String pincode = null;
        String city = null;
        String district = null;
        String state = null;
        String country = null;

        if (addressComponents.isArray()) {

            Iterator<JsonNode> iterator = addressComponents.elements();

            while (iterator.hasNext()) {

                JsonNode component = iterator.next();

                String longName = component.path("long_name").asText(null);

                JsonNode types = component.path("types");

                if (!types.isArray()) {
                    continue;
                }

                for (JsonNode typeNode : types) {

                    String type = typeNode.asText("");

                    switch (type) {

                        case "street_number":
                            streetNumber = longName;
                            break;

                        case "route":
                            route = longName;
                            break;

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
                            // sublocality is used for BOTH the area/locality field
                            // AND as a city fallback if "locality" wasn't present
                            if (areaLocality == null) {
                                areaLocality = longName;
                            }
                            if (city == null) {
                                city = longName;
                            }
                            break;

                        case "sublocality_level_2":
                        case "neighborhood":
                            if (areaLocality == null) {
                                areaLocality = longName;
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
            throw new GeocodingException("Geocoding service did not return a valid address.");
        }

        // Build addressLine1 from street number + route (e.g. "18th Main Road").
        // Falls back to areaLocality, then the first segment of the formatted
        // address, if Google didn't return a clean street-level component.
        String addressLine1 = buildAddressLine1(streetNumber, route, areaLocality, formattedAddress);

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

    private String buildAddressLine1(String streetNumber, String route, String areaLocality, String formattedAddress) {

        StringBuilder line1 = new StringBuilder();

        if (streetNumber != null && !streetNumber.isBlank()) {
            line1.append(streetNumber).append(" ");
        }
        if (route != null && !route.isBlank()) {
            line1.append(route);
        }

        String result = line1.toString().trim();

        if (!result.isEmpty()) {
            return result;
        }

        if (areaLocality != null && !areaLocality.isBlank()) {
            return areaLocality;
        }

        // Last resort: take the first comma-separated segment of the formatted address
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