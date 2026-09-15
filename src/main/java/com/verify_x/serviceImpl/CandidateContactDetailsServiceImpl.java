package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateContactDetailsRequest;
import com.verify_x.dto.CandidateContactDetailsResponse;

import com.verify_x.dto.CurrentAddressRequest;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateContactDetails;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateContactDetailsRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateContactDetailsService;
import com.verify_x.util.GoogleGeocodingClient;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateContactDetailsServiceImpl
        implements CandidateContactDetailsService {

    private static final long MAX_PHOTO_SIZE =
            5 * 1024 * 1024;

    private static final String DEFAULT_COUNTRY = "India";

    private final CandidateRepository candidateRepository;

    private final CandidateContactDetailsRepository
            candidateContactDetailsRepository;
    private final GoogleGeocodingClient googleGeocodingClient;

    // =========================================================
    // CREATE PERMANENT ADDRESS + CONTACT
    // =========================================================

    @Override
    public CandidateContactDetailsResponse createDetails(
            CandidateContactDetailsRequest request) {

        validatePermanentAddressRequest(request);

        Candidate candidate =
                getLoggedInCandidate();

        if (candidateContactDetailsRepository
                .existsByCandidate(candidate)) {

            throw new BadRequestException(
                    "Contact and address details already exist. Please update the existing details."
            );
        }

        CandidateContactDetails details =
                CandidateContactDetails.builder()
                        .candidate(candidate)

                        .permanentAddressLine1(
                                clean(request.getPermanentAddressLine1())
                        )
                        .permanentAddressLine2(
                                cleanNullable(
                                        request.getPermanentAddressLine2()
                                )
                        )
                        .permanentAreaLocality(
                                cleanNullable(
                                        request.getPermanentAreaLocality()
                                )
                        )
                        .permanentPincode(
                                clean(request.getPermanentPincode())
                        )
                        .permanentCity(
                                clean(request.getPermanentCity())
                        )
                        .permanentDistrict(
                                cleanNullable(
                                        request.getPermanentDistrict()
                                )
                        )
                        .permanentState(
                                clean(request.getPermanentState())
                        )
                        .permanentCountry(
                                clean(request.getPermanentCountry())
                        )
//                        .permanentLatitude(
//                                request.getPermanentLatitude()
//                        )
//                        .permanentLongitude(
//                                request.getPermanentLongitude()
//                        )

                        .emergencyContactNumber(
                                normalizePhone(
                                        request.getEmergencyContactNumber()
                                )
                        )
                        .referralContactNumber(
                                normalizeOptionalPhone(
                                        request.getReferralContactNumber()
                                )
                        )
                        .build();

        CandidateContactDetails saved =
                candidateContactDetailsRepository.save(details);

        log.info(
                "Candidate permanent address/contact created. candidateId={}, detailsId={}",
                candidate.getId(),
                saved.getId()
        );

        return mapToResponse(saved);
    }


    // =========================================================
    // UPDATE PERMANENT ADDRESS + CONTACT
    // =========================================================

    @Override
    public CandidateContactDetailsResponse updateDetails(
            CandidateContactDetailsRequest request) {

        validatePermanentAddressRequest(request);

        Candidate candidate =
                getLoggedInCandidate();

        CandidateContactDetails details =
                candidateContactDetailsRepository
                        .findByCandidate(candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Contact and address details not found."
                                )
                        );

        details.setPermanentAddressLine1(
                clean(request.getPermanentAddressLine1())
        );

        details.setPermanentAddressLine2(
                cleanNullable(request.getPermanentAddressLine2())
        );

        details.setPermanentAreaLocality(
                cleanNullable(request.getPermanentAreaLocality())
        );

        details.setPermanentPincode(
                clean(request.getPermanentPincode())
        );

        details.setPermanentCity(
                clean(request.getPermanentCity())
        );

        details.setPermanentDistrict(
                cleanNullable(request.getPermanentDistrict())
        );

        details.setPermanentState(
                clean(request.getPermanentState())
        );

        details.setPermanentCountry(
                clean(request.getPermanentCountry())
        );

//        details.setPermanentLatitude(
//                request.getPermanentLatitude()
//        );
//
//        details.setPermanentLongitude(
//                request.getPermanentLongitude()
//        );

        details.setEmergencyContactNumber(
                normalizePhone(
                        request.getEmergencyContactNumber()
                )
        );

        details.setReferralContactNumber(
                normalizeOptionalPhone(
                        request.getReferralContactNumber()
                )
        );

        CandidateContactDetails updated =
                candidateContactDetailsRepository.save(details);

        log.info(
                "Candidate permanent address/contact updated. candidateId={}, detailsId={}",
                candidate.getId(),
                updated.getId()
        );

        return mapToResponse(updated);
    }


    // =========================================================
    // CURRENT ADDRESS + GPS PHOTO
    // =========================================================

    @Override
    public CandidateContactDetailsResponse updateCurrentAddress(
            CurrentAddressRequest request,
            MultipartFile photo) {

        if (request == null) {

            throw new BadRequestException(
                    "GPS information is required."
            );
        }

        validateCoordinates(
                request.getLatitude(),
                request.getLongitude()
        );
        if (!request.isLocationConfirmed()) {
            throw new BadRequestException(
                    "Please confirm that the live photo and location are accurate."
            );
        }
        if (!request.isPhotoConfirmed()) {
            throw new BadRequestException(
                    "Please confirm the photo clearly shows the front or entrance of your house."
            );
        }

        validatePhoto(photo);

        Candidate candidate =
                getLoggedInCandidate();

        CandidateContactDetails details =
                candidateContactDetailsRepository
                        .findByCandidate(candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Please save permanent address and contact details first."
                                )
                        );

        /*
         * IMPORTANT:
         *
         * Here we will reverse-geocode the GPS coordinates
         * using Google Geocoding API.
         *
         * The returned address will be stored in:
         *
         * currentAddress
         * currentPincode
         * currentCity
         * currentDistrict
         * currentState
         * currentCountry
         */

//        CurrentAddressData addressData =
//                reverseGeocode(
//                        request.getLatitude(),
//                        request.getLongitude()
        GoogleGeocodingClient.GeocodeResult addressData =
                googleGeocodingClient.reverseGeocode(
                        request.getLatitude(),
                        request.getLongitude()
                );

        try {

            details.setCurrentAddress(
                    addressData.address()
            );

            details.setCurrentPincode(
                    addressData.pincode()
            );

            details.setCurrentCity(
                    addressData.city()
            );
            details.setCurrentAddressLine1(addressData.addressLine1());
            details.setCurrentAreaLocality(addressData.areaLocality());
            details.setCurrentDistrict(
                    addressData.district()
            );

            details.setCurrentState(
                    addressData.state()
            );

//            details.setCurrentCountry(
//                    addressData.country()
//            );
            details.setCurrentCountry(
                    addressData.country() != null
                            ? addressData.country()
                            : DEFAULT_COUNTRY
            );

            details.setCurrentLatitude(
                    request.getLatitude()
            );

            details.setCurrentLongitude(
                    request.getLongitude()
            );

            details.setCurrentAddressPhoto(
                    photo.getBytes()
            );

            details.setCurrentAddressPhotoName(
                    sanitizeFileName(
                            photo.getOriginalFilename()
                    )
            );

            details.setCurrentAddressPhotoContentType(
                    photo.getContentType()
            );

            CandidateContactDetails updated =
                    candidateContactDetailsRepository.save(details);

            log.info(
                    "Current address captured. candidateId={}, detailsId={}, lat={}, lng={}",
                    candidate.getId(),
                    details.getId(),
                    request.getLatitude(),
                    request.getLongitude()
            );

            return mapToResponse(updated);

        } catch (IOException ex) {

            log.error(
                    "Failed to store current address photo. candidateId={}",
                    candidate.getId(),
                    ex
            );

            throw new BadRequestException(
                    "Unable to process current address photo."
            );
        }
    }


    // =========================================================
    // GET MY DETAILS
    // =========================================================

    @Override
    public CandidateContactDetailsResponse getMyDetails() {

        Candidate candidate =
                getLoggedInCandidate();

        CandidateContactDetails details =
                candidateContactDetailsRepository
                        .findByCandidate(candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Contact and address details not found."
                                )
                        );

        return mapToResponse(details);
    }


    // =========================================================
    // HR / ADMIN GET DETAILS
    // =========================================================

    @Override
    public CandidateContactDetailsResponse getDetailsByCandidateId(
            Long candidateId) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required."
            );
        }

        if (!candidateRepository.existsById(candidateId)) {

            throw new ResourceNotFoundException(
                    "Candidate",
                    candidateId
            );
        }

        CandidateContactDetails details =
                candidateContactDetailsRepository
                        .findByCandidateId(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Contact and address details not found for candidate."
                                )
                        );

        return mapToResponse(details);
    }


    // =========================================================
    // CURRENT ADDRESS PHOTO
    // =========================================================

    @Override
    public Resource getCurrentAddressPhoto(
            Long candidateId) {

        CandidateContactDetails details =
                candidateContactDetailsRepository
                        .findByCandidateId(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Contact and address details not found."
                                )
                        );

        if (details.getCurrentAddressPhoto() == null) {

            throw new ResourceNotFoundException(
                    "Current address photo not found."
            );
        }

        return new ByteArrayResource(
                details.getCurrentAddressPhoto()
        );
    }


    // =========================================================
    // VALIDATE PERMANENT ADDRESS
    // =========================================================

    private void validatePermanentAddressRequest(
            CandidateContactDetailsRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Contact and address details are required."
            );
        }

        if (isBlank(request.getPermanentAddressLine1())) {

            throw new BadRequestException(
                    "Permanent address line 1 is required."
            );
        }

        if (isBlank(request.getPermanentPincode())) {

            throw new BadRequestException(
                    "Permanent pincode is required."
            );
        }

        if (!request.getPermanentPincode()
                .matches("^[1-9][0-9]{5}$")) {

            throw new BadRequestException(
                    "Permanent pincode must be a valid 6-digit Indian pincode."
            );
        }

        if (isBlank(request.getPermanentCity())) {

            throw new BadRequestException(
                    "Permanent city is required."
            );
        }

        if (isBlank(request.getPermanentState())) {

            throw new BadRequestException(
                    "Permanent state is required."
            );
        }

        if (isBlank(request.getPermanentCountry())) {

            throw new BadRequestException(
                    "Permanent country is required."
            );
        }

        validatePhone(
                request.getEmergencyContactNumber(),
                "Emergency contact number"
        );

        if (!isBlank(request.getReferralContactNumber())) {

            validatePhone(
                    request.getReferralContactNumber(),
                    "Referral contact number"
            );
        }

//        validateCoordinatesIfProvided(
//                request.getPermanentLatitude(),
//                request.getPermanentLongitude()
//        );
    }


    // =========================================================
    // GPS VALIDATION
    // =========================================================

    private void validateCoordinates(
            Double latitude,
            Double longitude) {

        if (latitude == null ||
                longitude == null) {

            throw new BadRequestException(
                    "GPS latitude and longitude are required."
            );
        }

        if (latitude < -90 || latitude > 90) {

            throw new BadRequestException(
                    "Invalid latitude."
            );
        }

        if (longitude < -180 || longitude > 180) {

            throw new BadRequestException(
                    "Invalid longitude."
            );
        }
    }


    private void validateCoordinatesIfProvided(
            Double latitude,
            Double longitude) {

        if (latitude == null && longitude == null) {
            return;
        }

        validateCoordinates(latitude, longitude);
    }


    // =========================================================
    // PHOTO VALIDATION
    // =========================================================

    private void validatePhoto(
            MultipartFile photo) {

        if (photo == null ||
                photo.isEmpty()) {

            throw new BadRequestException(
                    "Current address photo is required."
            );
        }

        if (photo.getSize() > MAX_PHOTO_SIZE) {

            throw new BadRequestException(
                    "Current address photo cannot exceed 5 MB."
            );
        }

        String contentType =
                photo.getContentType();

        if (contentType == null ||
                (!contentType.equalsIgnoreCase("image/jpeg") &&
                        !contentType.equalsIgnoreCase("image/png"))) {

            throw new BadRequestException(
                    "Only JPG, JPEG and PNG photos are allowed."
            );
        }
    }


    // =========================================================
    // PHONE VALIDATION
    // =========================================================

    private void validatePhone(
            String phone,
            String fieldName) {

        if (isBlank(phone)) {

            throw new BadRequestException(
                    fieldName + " is required."
            );
        }

        String normalized =
                normalizePhone(phone);

        if (!normalized.matches(
                "^[6-9][0-9]{9}$")) {

            throw new BadRequestException(
                    fieldName +
                            " must be a valid 10-digit Indian mobile number."
            );
        }
    }


    private String normalizePhone(
            String phone) {

        if (phone == null) {
            return null;
        }

        return phone.trim()
                .replaceAll("\\s+", "");
    }


    private String normalizeOptionalPhone(
            String phone) {

        if (isBlank(phone)) {
            return null;
        }

        return normalizePhone(phone);
    }


    // =========================================================
    // STRING CLEANING
    // =========================================================

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }


    private String cleanNullable(String value) {

        if (isBlank(value)) {
            return null;
        }

        return clean(value);
    }


    private boolean isBlank(String value) {

        return value == null ||
                value.trim().isEmpty();
    }


    // =========================================================
    // FILE NAME SANITIZATION
    // =========================================================

    private String sanitizeFileName(
            String originalFileName) {

        if (originalFileName == null ||
                originalFileName.isBlank()) {

            return "current-address-photo";
        }

        String fileName =
                originalFileName.replace("\\", "/");

        fileName =
                fileName.substring(
                        fileName.lastIndexOf("/") + 1
                );

        fileName =
                fileName.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        if (fileName.length() > 255) {

            fileName =
                    fileName.substring(
                            fileName.length() - 255
                    );
        }

        return fileName;
    }


    // =========================================================
    // AUTHENTICATED CANDIDATE
    // =========================================================

    private Candidate getLoggedInCandidate() {

        UserPrincipal principal =
                getAuthenticatedPrincipal();

        Long candidateId =
                principal.getUserId();

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate information is missing from authentication."
            );
        }

        return candidateRepository
                .findById(candidateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate",
                                candidateId
                        )
                );
    }


    private UserPrincipal getAuthenticatedPrincipal() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getPrincipal() == null) {

            throw new BadRequestException(
                    "User is not authenticated."
            );
        }

        if (!(authentication.getPrincipal()
                instanceof UserPrincipal)) {

            throw new BadRequestException(
                    "Invalid authentication details."
            );
        }

        return (UserPrincipal)
                authentication.getPrincipal();
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private CandidateContactDetailsResponse mapToResponse(
            CandidateContactDetails details) {

        return CandidateContactDetailsResponse.builder()

                .id(details.getId())

                .candidateId(
                        details.getCandidate().getId()
                )

                .permanentAddressLine1(
                        details.getPermanentAddressLine1()
                )

                .permanentAddressLine2(
                        details.getPermanentAddressLine2()
                )

                .permanentAreaLocality(
                        details.getPermanentAreaLocality()
                )

                .permanentPincode(
                        details.getPermanentPincode()
                )

                .permanentCity(
                        details.getPermanentCity()
                )

                .permanentDistrict(
                        details.getPermanentDistrict()
                )

                .permanentState(
                        details.getPermanentState()
                )

                .permanentCountry(
                        details.getPermanentCountry()
                )

//                .permanentLatitude(
//                        details.getPermanentLatitude()
//                )
//
//                .permanentLongitude(
//                        details.getPermanentLongitude()
//                )

                .currentAddress(
                        details.getCurrentAddress()
                )

                .currentPincode(
                        details.getCurrentPincode()
                )

                .currentCity(
                        details.getCurrentCity()
                )

                .currentDistrict(
                        details.getCurrentDistrict()
                )

                .currentState(
                        details.getCurrentState()
                )

                .currentCountry(
                        details.getCurrentCountry()
                )

                .currentLatitude(
                        details.getCurrentLatitude()
                )

                .currentLongitude(
                        details.getCurrentLongitude()
                )

                .currentAddressPhotoName(
                        details.getCurrentAddressPhotoName()
                )

                .currentAddressPhotoContentType(
                        details.getCurrentAddressPhotoContentType()
                )

                .currentAddressPhotoAvailable(
                        details.getCurrentAddressPhoto() != null
                )

                .emergencyContactNumber(
                        details.getEmergencyContactNumber()
                )

                .referralContactNumber(
                        details.getReferralContactNumber()
                )

                .createdAt(
                        details.getCreatedAt()
                )

                .updatedAt(
                        details.getUpdatedAt()
                )

                .build();
    }


    // =========================================================
    // GOOGLE REVERSE GEOCODING
    // =========================================================

//    private CurrentAddressData reverseGeocode(
//            Double latitude,
//            Double longitude) {
//
//        /*
//         * Implement Google Geocoding API here.
//         *
//         * Do NOT accept currentAddress from frontend.
//         *
//         * GPS coordinates are the source of truth.
//         */
//
//        throw new UnsupportedOperationException(
//                "Google reverse geocoding is not configured yet."
//        );
//    }


    // =========================================================
    // INTERNAL ADDRESS RECORD
    // =========================================================

//    private record CurrentAddressData(
//            String address,
//            String pincode,
//            String city,
//            String district,
//            String state,
//            String country
//    )
    {
    }
}