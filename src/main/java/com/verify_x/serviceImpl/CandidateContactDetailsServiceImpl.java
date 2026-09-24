package com.verify_x.serviceImpl;

import com.verify_x.dto.CurrentAddressRequest;
import com.verify_x.dto.CurrentAddressResponse;
import com.verify_x.dto.PermanentAddressRequest;
import com.verify_x.dto.PermanentAddressResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateContactDetails;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateContactDetailsRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateContactDetailsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateContactDetailsServiceImpl implements CandidateContactDetailsService {

    private final CandidateRepository candidateRepository;
    private final CandidateContactDetailsRepository candidateContactDetailsRepository;

    // =========================================================
    // PERMANENT ADDRESS - CREATE
    // =========================================================

    @Override
    public PermanentAddressResponse createPermanentAddress(PermanentAddressRequest request) {

        if (request == null) {
            throw new BadRequestException("Permanent address details are required.");
        }

        validatePhone(request.getEmergencyContactNumber(), "Emergency contact number");
        if (!isBlank(request.getReferralContactNumber())) {
            validatePhone(request.getReferralContactNumber(), "Referral contact number");
        }

        Candidate candidate = getLoggedInCandidate();

        if (candidateContactDetailsRepository.existsByCandidate(candidate)) {
            throw new BadRequestException(
                    "Permanent address already exists. Please use update instead."
            );
        }

        CandidateContactDetails details = CandidateContactDetails.builder()
                .candidate(candidate)
                .permanentAddressLine1(clean(request.getPermanentAddressLine1()))
                .permanentAddressLine2(cleanNullable(request.getPermanentAddressLine2()))
                .permanentAreaLocality(cleanNullable(request.getPermanentAreaLocality()))
                .permanentPincode(clean(request.getPermanentPincode()))
                .permanentCity(clean(request.getPermanentCity()))
                .permanentDistrict(cleanNullable(request.getPermanentDistrict()))
                .permanentState(clean(request.getPermanentState()))
                .permanentCountry(clean(request.getPermanentCountry()))
                .emergencyContactNumber(normalizePhone(request.getEmergencyContactNumber()))
                .referralContactNumber(normalizeOptionalPhone(request.getReferralContactNumber()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CandidateContactDetails saved = candidateContactDetailsRepository.save(details);

        log.info("Permanent address created. candidateId={}, detailsId={}", candidate.getId(), saved.getId());

        return mapToPermanentResponse(saved);
    }

    // =========================================================
    // PERMANENT ADDRESS - UPDATE
    // =========================================================

    @Override
    public PermanentAddressResponse updatePermanentAddress(PermanentAddressRequest request) {

        if (request == null) {
            throw new BadRequestException("Permanent address details are required.");
        }

        validatePhone(request.getEmergencyContactNumber(), "Emergency contact number");
        if (!isBlank(request.getReferralContactNumber())) {
            validatePhone(request.getReferralContactNumber(), "Referral contact number");
        }

        Candidate candidate = getLoggedInCandidate();

        CandidateContactDetails details = candidateContactDetailsRepository
                .findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permanent address not found. Please create it first."
                ));

        details.setPermanentAddressLine1(clean(request.getPermanentAddressLine1()));
        details.setPermanentAddressLine2(cleanNullable(request.getPermanentAddressLine2()));
        details.setPermanentAreaLocality(cleanNullable(request.getPermanentAreaLocality()));
        details.setPermanentPincode(clean(request.getPermanentPincode()));
        details.setPermanentCity(clean(request.getPermanentCity()));
        details.setPermanentDistrict(cleanNullable(request.getPermanentDistrict()));
        details.setPermanentState(clean(request.getPermanentState()));
        details.setPermanentCountry(clean(request.getPermanentCountry()));
        details.setEmergencyContactNumber(normalizePhone(request.getEmergencyContactNumber()));
        details.setReferralContactNumber(normalizeOptionalPhone(request.getReferralContactNumber()));
        details.setUpdatedAt(LocalDateTime.now());
        details.setCreatedAt(LocalDateTime.now());

        CandidateContactDetails updated = candidateContactDetailsRepository.save(details);

        log.info("Permanent address updated. candidateId={}, detailsId={}", candidate.getId(), updated.getId());

        return mapToPermanentResponse(updated);
    }

    // =========================================================
    // PERMANENT ADDRESS - GET
    // =========================================================

    @Override
    public PermanentAddressResponse getMyPermanentAddress() {

        Candidate candidate = getLoggedInCandidate();

        CandidateContactDetails details = candidateContactDetailsRepository
                .findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Permanent address not found."));

        return mapToPermanentResponse(details);
    }

    // =========================================================
    // CURRENT ADDRESS - CREATE OR UPDATE (single idempotent method)
    // =========================================================

    @Override
    public CurrentAddressResponse createOrUpdateCurrentAddress(CurrentAddressRequest request) {

        if (request == null) {
            throw new BadRequestException("Current address details are required.");
        }

//        validateCoordinatesIfProvided(request.getCurrentLatitude(), request.getCurrentLongitude());

        Candidate candidate = getLoggedInCandidate();

        CandidateContactDetails details = candidateContactDetailsRepository
                .findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Please save permanent address first before adding current address."
                ));

        details.setCurrentAddressLine1(clean(request.getCurrentAddressLine1()));
        details.setCurrentAddressLine2(cleanNullable(request.getCurrentAddressLine2()));
        details.setCurrentAreaLocality(cleanNullable(request.getCurrentAreaLocality()));
        details.setCurrentPincode(clean(request.getCurrentPincode()));
        details.setCurrentCity(clean(request.getCurrentCity()));
        details.setCurrentDistrict(cleanNullable(request.getCurrentDistrict()));
        details.setCurrentState(clean(request.getCurrentState()));
        details.setCurrentCountry(clean(request.getCurrentCountry()));
        details.setCurrentAddress(buildCombinedCurrentAddress(request));
//        details.setCurrentLatitude(request.getCurrentLatitude());
//        details.setCurrentLongitude(request.getCurrentLongitude());
        details.setUpdatedAt(LocalDateTime.now());
        details.setCreatedAt(LocalDateTime.now());

        CandidateContactDetails updated = candidateContactDetailsRepository.save(details);

        log.info("Current address saved. candidateId={}, detailsId={}", candidate.getId(), updated.getId());

        return mapToCurrentResponse(updated);
    }

    // =========================================================
    // CURRENT ADDRESS - GET
    // =========================================================

    @Override
    public CurrentAddressResponse getMyCurrentAddress() {

        Candidate candidate = getLoggedInCandidate();

        CandidateContactDetails details = candidateContactDetailsRepository
                .findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Contact details not found."));

        if (isBlank(details.getCurrentAddressLine1())) {
            throw new ResourceNotFoundException("Current address has not been set yet.");
        }

        return mapToCurrentResponse(details);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String buildCombinedCurrentAddress(CurrentAddressRequest request) {

        StringBuilder sb = new StringBuilder();

        appendIfPresent(sb, request.getCurrentAddressLine1());
        appendIfPresent(sb, request.getCurrentAddressLine2());
        appendIfPresent(sb, request.getCurrentAreaLocality());
        appendIfPresent(sb, request.getCurrentCity());
        appendIfPresent(sb, request.getCurrentDistrict());
        appendIfPresent(sb, request.getCurrentState());
        appendIfPresent(sb, request.getCurrentPincode());
        appendIfPresent(sb, request.getCurrentCountry());

        return sb.toString();
    }

    private void appendIfPresent(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value.trim());
        }
    }

    private void validateCoordinatesIfProvided(Double latitude, Double longitude) {

        if (latitude == null && longitude == null) {
            return;
        }

        if (latitude == null || longitude == null) {
            throw new BadRequestException("Both latitude and longitude must be provided together.");
        }

        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("Invalid latitude.");
        }

        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("Invalid longitude.");
        }
    }

    private void validatePhone(String phone, String fieldName) {

        if (isBlank(phone)) {
            throw new BadRequestException(fieldName + " is required.");
        }

        String normalized = normalizePhone(phone);

        if (!normalized.matches("^[6-9][0-9]{9}$")) {
            throw new BadRequestException(
                    fieldName + " must be a valid 10-digit Indian mobile number."
            );
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.trim().replaceAll("\\s+", "");
    }

    private String normalizeOptionalPhone(String phone) {
        if (isBlank(phone)) return null;
        return normalizePhone(phone);
    }

    private String clean(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ");
    }

    private String cleanNullable(String value) {
        if (isBlank(value)) return null;
        return clean(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Candidate getLoggedInCandidate() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadRequestException("User is not authenticated.");
        }

        Long candidateId = principal.getUserId();

        if (candidateId == null) {
            throw new BadRequestException("Candidate information is missing from authentication.");
        }

        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));
    }

    // =========================================================
    // RESPONSE MAPPERS
    // =========================================================

    private PermanentAddressResponse mapToPermanentResponse(CandidateContactDetails details) {

        return PermanentAddressResponse.builder()
                .id(details.getId())
                .candidateId(details.getCandidate().getId())
                .permanentAddressLine1(details.getPermanentAddressLine1())
                .permanentAddressLine2(details.getPermanentAddressLine2())
                .permanentAreaLocality(details.getPermanentAreaLocality())
                .permanentPincode(details.getPermanentPincode())
                .permanentCity(details.getPermanentCity())
                .permanentDistrict(details.getPermanentDistrict())
                .permanentState(details.getPermanentState())
                .permanentCountry(details.getPermanentCountry())
                .emergencyContactNumber(details.getEmergencyContactNumber())
                .referralContactNumber(details.getReferralContactNumber())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }

    private CurrentAddressResponse mapToCurrentResponse(CandidateContactDetails details) {

        return CurrentAddressResponse.builder()
                .id(details.getId())
                .candidateId(details.getCandidate().getId())
                .currentAddress(details.getCurrentAddress())
                .currentAddressLine1(details.getCurrentAddressLine1())
                .currentAddressLine2(details.getCurrentAddressLine2())
                .currentAreaLocality(details.getCurrentAreaLocality())
                .currentPincode(details.getCurrentPincode())
                .currentCity(details.getCurrentCity())
                .currentDistrict(details.getCurrentDistrict())
                .currentState(details.getCurrentState())
                .currentCountry(details.getCurrentCountry())
//                .currentLatitude(details.getCurrentLatitude())
//                .currentLongitude(details.getCurrentLongitude())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }
    // =========================================================
// HR / ADMIN - PERMANENT ADDRESS
// =========================================================

    @Override
    public PermanentAddressResponse getPermanentAddressByCandidateId(Long candidateId) {

        if (candidateId == null) {
            throw new BadRequestException("Candidate ID is required.");
        }

        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate", candidateId);
        }

        CandidateContactDetails details = candidateContactDetailsRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permanent address not found for candidate."
                ));

        return mapToPermanentResponse(details);
    }

// =========================================================
// HR / ADMIN - CURRENT ADDRESS
// =========================================================

    @Override
    public CurrentAddressResponse getCurrentAddressByCandidateId(Long candidateId) {

        if (candidateId == null) {
            throw new BadRequestException("Candidate ID is required.");
        }

        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate", candidateId);
        }

        CandidateContactDetails details = candidateContactDetailsRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contact details not found for candidate."
                ));

        if (isBlank(details.getCurrentAddressLine1())) {
            throw new ResourceNotFoundException("Current address has not been set for this candidate.");
        }

        return mapToCurrentResponse(details);
    }
}