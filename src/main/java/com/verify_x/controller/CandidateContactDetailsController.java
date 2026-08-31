package com.verify_x.controller;

import com.verify_x.dto.CandidateContactDetailsRequest;
import com.verify_x.dto.CandidateContactDetailsResponse;
import com.verify_x.dto.CurrentAddressRequest;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateContactDetailsService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate/contact-details")
@RequiredArgsConstructor
public class CandidateContactDetailsController {

    private final CandidateContactDetailsService
            candidateContactDetailsService;


    // =========================================================
    // CREATE PERMANENT ADDRESS + CONTACT
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<
            ApiResponse<CandidateContactDetailsResponse>>
    createDetails(
            @Valid
            @RequestBody
            CandidateContactDetailsRequest request) {

        CandidateContactDetailsResponse response =
                candidateContactDetailsService
                        .createDetails(request);

        return ResponseEntity
                .status(201)
                .body(
                        ApiResponse.success(
                                "Permanent address and contact details saved successfully.",
                                response
                        )
                );
    }


    // =========================================================
    // UPDATE PERMANENT ADDRESS + CONTACT
    // =========================================================

    @PutMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<
            ApiResponse<CandidateContactDetailsResponse>>
    updateDetails(
            @Valid
            @RequestBody
            CandidateContactDetailsRequest request) {

        CandidateContactDetailsResponse response =
                candidateContactDetailsService
                        .updateDetails(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permanent address and contact details updated successfully.",
                        response
                )
        );
    }


    // =========================================================
    // CURRENT ADDRESS GPS + PHOTO
    // =========================================================

    @PostMapping(
            value = "/current-address",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<
            ApiResponse<CandidateContactDetailsResponse>>
    updateCurrentAddress(

            @RequestPart("data")
            CurrentAddressRequest request,

            @RequestPart("photo")
            MultipartFile photo) {

        CandidateContactDetailsResponse response =
                candidateContactDetailsService
                        .updateCurrentAddress(
                                request,
                                photo
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current address captured successfully.",
                        response
                )
        );
    }


    // =========================================================
    // CANDIDATE - MY DETAILS
    // =========================================================

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<
            ApiResponse<CandidateContactDetailsResponse>>
    getMyDetails() {

        CandidateContactDetailsResponse response =
                candidateContactDetailsService
                        .getMyDetails();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Candidate contact and address details fetched successfully.",
                        response
                )
        );
    }


    // =========================================================
    // HR / ADMIN
    // =========================================================

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<
            ApiResponse<CandidateContactDetailsResponse>>
    getDetailsByCandidateId(
            @PathVariable Long candidateId) {

        CandidateContactDetailsResponse response =
                candidateContactDetailsService
                        .getDetailsByCandidateId(
                                candidateId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Candidate contact and address details fetched successfully.",
                        response
                )
        );
    }


    // =========================================================
    // CURRENT ADDRESS PHOTO
    // =========================================================

    @GetMapping("/candidate/{candidateId}/current-address-photo")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<Resource>
    getCurrentAddressPhoto(
            @PathVariable Long candidateId) {

        Resource resource =
                candidateContactDetailsService
                        .getCurrentAddressPhoto(
                                candidateId
                        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline"
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(resource);
    }
}