//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.CandidateContactDetailsRequest1;
//import com.verify_x.dto.CandidateContactDetailsResponse1;
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.CandidateContactDetail;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.exception.ResourceNotFoundException;
//import com.verify_x.jwt.UserPrincipal;
//import com.verify_x.repository.CandidateContactDetailsRepository1;
//import com.verify_x.repository.CandidateRepository;
//import com.verify_x.services.CandidateContactDetailsServices;
//
//import jakarta.transaction.Transactional;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class CandidateContactDetailsServiceImpls
//        implements CandidateContactDetailsServices {
//
//    private final CandidateRepository candidateRepository;
//
//    private final CandidateContactDetailsRepository1
//            candidateContactDetailsRepository;
//
//
//    // =========================================================
//    // CREATE DETAILS
//    // =========================================================
//
//    @Override
//    public CandidateContactDetailsResponse1 createDetails(
//            CandidateContactDetailsRequest1 request) {
//
//        validateRequest(request);
//
//        Candidate candidate = getLoggedInCandidate();
//
//        /*
//         * Only one record is allowed per candidate.
//         */
//        if (candidateContactDetailsRepository
//                .existsByCandidate(candidate)) {
//
//            throw new BadRequestException(
//                    "Contact and address details already exist. " +
//                            "Please update the existing details."
//            );
//        }
//
//        CandidateContactDetail details =
//                CandidateContactDetail.builder()
//                        .candidate(candidate)
//
//                        .permanentAddress(
//                                normalizeAddress(
//                                        request.getPermanentAddress()
//                                )
//                        )
//
//                        .residentialAddress(
//                                normalizeAddress(
//                                        request.getResidentialAddress()
//                                )
//                        )
//
//                        .emergencyContactNumber(
//                                normalizePhoneNumber(
//                                        request.getEmergencyContactNumber()
//                                )
//                        )
//
//                        .referralContactNumber(
//                                normalizeOptionalPhoneNumber(
//                                        request.getReferralContactNumber()
//                                )
//                        )
//
//                        .build();
//
//        CandidateContactDetail saved =
//                candidateContactDetailsRepository.save(details);
//
//        log.info(
//                "Candidate contact details created. candidateId={}, detailsId={}",
//                candidate.getId(),
//                saved.getId()
//        );
//
//        return mapToResponse(saved);
//    }
//
//
//    // =========================================================
//    // UPDATE DETAILS
//    // =========================================================
//
//    @Override
//    public CandidateContactDetailsResponse1 updateDetails(
//            CandidateContactDetailsRequest1 request) {
//
//        validateRequest(request);
//
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateContactDetail details =
//                candidateContactDetailsRepository
//                        .findByCandidate(candidate)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Contact and address details not found. " +
//                                                "Please create your contact details first."
//                                )
//                        );
//
//        /*
//         * Update permanent address.
//         */
//        details.setPermanentAddress(
//                normalizeAddress(
//                        request.getPermanentAddress()
//                )
//        );
//
//        /*
//         * Update current/residential address.
//         */
//        details.setResidentialAddress(
//                normalizeAddress(
//                        request.getResidentialAddress()
//                )
//        );
//
//        /*
//         * Update mandatory emergency contact.
//         */
//        details.setEmergencyContactNumber(
//                normalizePhoneNumber(
//                        request.getEmergencyContactNumber()
//                )
//        );
//
//        /*
//         * Referral contact is optional.
//         *
//         * Empty string is converted to null.
//         */
//        details.setReferralContactNumber(
//                normalizeOptionalPhoneNumber(
//                        request.getReferralContactNumber()
//                )
//        );
//
//        CandidateContactDetail updated =
//                candidateContactDetailsRepository.save(details);
//
//        log.info(
//                "Candidate contact details updated. candidateId={}, detailsId={}",
//                candidate.getId(),
//                updated.getId()
//        );
//
//        return mapToResponse(updated);
//    }
//
//
//    // =========================================================
//    // GET MY DETAILS
//    // =========================================================
//
//    @Override
//    public CandidateContactDetailsResponse1 getMyDetails() {
//
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateContactDetail details =
//                candidateContactDetailsRepository
//                        .findByCandidate(candidate)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Contact and address details not found."
//                                )
//                        );
//
//        return mapToResponse(details);
//    }
//
//
//    // =========================================================
//    // GET DETAILS BY CANDIDATE ID
//    // HR / ADMIN
//    // =========================================================
//
//    @Override
//    public CandidateContactDetailsResponse1 getDetailsByCandidateId(
//            Long candidateId) {
//
//        if (candidateId == null) {
//
//            throw new BadRequestException(
//                    "Candidate ID is required."
//            );
//        }
//
//        Candidate candidate =
//                candidateRepository.findById(candidateId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Candidate",
//                                        candidateId
//                                )
//                        );
//
//        CandidateContactDetail details =
//                candidateContactDetailsRepository
//                        .findByCandidate(candidate)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Contact and address details not found for candidate."
//                                )
//                        );
//
//        return mapToResponse(details);
//    }
//
//
//    // =========================================================
//    // GET LOGGED-IN CANDIDATE
//    // =========================================================
//
//    private Candidate getLoggedInCandidate() {
//
//        UserPrincipal principal =
//                getAuthenticatedPrincipal();
//
//        Long candidateId =
//                principal.getUserId();
//
//        if (candidateId == null) {
//
//            throw new BadRequestException(
//                    "Candidate information is missing from authentication."
//            );
//        }
//
//        return candidateRepository
//                .findById(candidateId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Candidate",
//                                candidateId
//                        )
//                );
//    }
//
//
//    // =========================================================
//    // GET AUTHENTICATED PRINCIPAL
//    // =========================================================
//
//    private UserPrincipal getAuthenticatedPrincipal() {
//
//        Authentication authentication =
//                SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//
//        if (authentication == null ||
//                authentication.getPrincipal() == null) {
//
//            throw new BadRequestException(
//                    "User is not authenticated."
//            );
//        }
//
//        if (!(authentication.getPrincipal()
//                instanceof UserPrincipal)) {
//
//            throw new BadRequestException(
//                    "Invalid authentication details."
//            );
//        }
//
//        return (UserPrincipal)
//                authentication.getPrincipal();
//    }
//
//
//    // =========================================================
//    // REQUEST VALIDATION
//    // =========================================================
//
//    private void validateRequest(
//            CandidateContactDetailsRequest1 request) {
//
//        if (request == null) {
//
//            throw new BadRequestException(
//                    "Contact and address details are required."
//            );
//        }
//
//        // -----------------------------------------------------
//        // Permanent Address
//        // -----------------------------------------------------
//
//        if (isBlank(request.getPermanentAddress())) {
//
//            throw new BadRequestException(
//                    "Permanent address is required."
//            );
//        }
//
//        String permanentAddress =
//                normalizeAddress(
//                        request.getPermanentAddress()
//                );
//
//        if (permanentAddress.length() < 5) {
//
//            throw new BadRequestException(
//                    "Permanent address must contain at least 5 characters."
//            );
//        }
//
//        if (permanentAddress.length() > 1000) {
//
//            throw new BadRequestException(
//                    "Permanent address cannot exceed 1000 characters."
//            );
//        }
//
//        // -----------------------------------------------------
//        // Residential Address
//        // -----------------------------------------------------
//
//        if (isBlank(request.getResidentialAddress())) {
//
//            throw new BadRequestException(
//                    "Residential address is required."
//            );
//        }
//
//        String residentialAddress =
//                normalizeAddress(
//                        request.getResidentialAddress()
//                );
//
//        if (residentialAddress.length() < 5) {
//
//            throw new BadRequestException(
//                    "Residential address must contain at least 5 characters."
//            );
//        }
//
//        if (residentialAddress.length() > 1000) {
//
//            throw new BadRequestException(
//                    "Residential address cannot exceed 1000 characters."
//            );
//        }
//
//        // -----------------------------------------------------
//        // Emergency Contact
//        // -----------------------------------------------------
//
//        if (isBlank(request.getEmergencyContactNumber())) {
//
//            throw new BadRequestException(
//                    "Emergency contact number is required."
//            );
//        }
//
//        validateIndianMobileNumber(
//                request.getEmergencyContactNumber(),
//                "Emergency contact number"
//        );
//
//        // -----------------------------------------------------
//        // Referral Contact - OPTIONAL
//        // -----------------------------------------------------
//
//        if (!isBlank(request.getReferralContactNumber())) {
//
//            validateIndianMobileNumber(
//                    request.getReferralContactNumber(),
//                    "Referral contact number"
//            );
//        }
//    }
//
//
//    // =========================================================
//    // INDIAN MOBILE NUMBER VALIDATION
//    // =========================================================
//
//    private void validateIndianMobileNumber(
//            String phoneNumber,
//            String fieldName) {
//
//        String normalized =
//                normalizePhoneNumber(phoneNumber);
//
//        if (!normalized.matches(
//                "^[6-9][0-9]{9}$")) {
//
//            throw new BadRequestException(
//                    fieldName +
//                            " must be a valid 10-digit Indian mobile number."
//            );
//        }
//    }
//
//
//    // =========================================================
//    // NORMALIZE PHONE NUMBER
//    // =========================================================
//
//    private String normalizePhoneNumber(
//            String phoneNumber) {
//
//        if (phoneNumber == null) {
//
//            return null;
//        }
//
//        return phoneNumber
//                .trim()
//                .replaceAll("\\s+", "");
//    }
//
//
//    // =========================================================
//    // OPTIONAL PHONE NUMBER
//    // =========================================================
//
//    private String normalizeOptionalPhoneNumber(
//            String phoneNumber) {
//
//        if (phoneNumber == null ||
//                phoneNumber.isBlank()) {
//
//            return null;
//        }
//
//        return normalizePhoneNumber(phoneNumber);
//    }
//
//
//    // =========================================================
//    // NORMALIZE ADDRESS
//    // =========================================================
//
//    private String normalizeAddress(
//            String address) {
//
//        if (address == null) {
//
//            return null;
//        }
//
//        return address
//                .trim()
//                .replaceAll("\\s+", " ");
//    }
//
//
//    // =========================================================
//    // BLANK CHECK
//    // =========================================================
//
//    private boolean isBlank(String value) {
//
//        return value == null ||
//                value.trim().isEmpty();
//    }
//
//
//    // =========================================================
//    // DTO MAPPING
//    // =========================================================
//
//    private CandidateContactDetailsResponse1 mapToResponse(
//            @MonotonicNonNull CandidateContactDetail details) {
//
//        return CandidateContactDetailsResponse1.builder()
//
//                .id(details.getId())
//
//                .candidateId(
//                        details.getCandidate().getId()
//                )
//
//                .permanentAddress(
//                        details.getPermanentAddress()
//                )
//
//                .residentialAddress(
//                        details.getResidentialAddress()
//                )
//
//                .emergencyContactNumber(
//                        details.getEmergencyContactNumber()
//                )
//
//                .referralContactNumber(
//                        details.getReferralContactNumber()
//                )
//
//                .createdAt(
//                        details.getCreatedAt()
//                )
//
//                .updatedAt(
//                        details.getUpdatedAt()
//                )
//
//                .build();
//    }
//}