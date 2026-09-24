package com.verify_x.controller;

import com.verify_x.dto.CurrentAddressRequest;
import com.verify_x.dto.CurrentAddressResponse;
import com.verify_x.dto.PermanentAddressRequest;
import com.verify_x.dto.PermanentAddressResponse;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateContactDetailsService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidate/contact-details")
@RequiredArgsConstructor
@Slf4j
public class CandidateContactDetailsController {

    private final CandidateContactDetailsService candidateContactDetailsService;

    // =========================================================
    // PERMANENT ADDRESS
    // =========================================================

    @PostMapping("/permanent-address")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<PermanentAddressResponse>> createPermanentAddress(
            @Valid @RequestBody PermanentAddressRequest request) {

        PermanentAddressResponse response =
                candidateContactDetailsService.createPermanentAddress(request);

        return ResponseEntity.status(201).body(
                ApiResponse.success("Permanent address saved successfully.", response)
        );
    }

    @PutMapping("/permanent-address")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<PermanentAddressResponse>> updatePermanentAddress(
            @Valid @RequestBody PermanentAddressRequest request) {

        PermanentAddressResponse response =
                candidateContactDetailsService.updatePermanentAddress(request);

        return ResponseEntity.ok(
                ApiResponse.success("Permanent address updated successfully.", response)
        );
    }

    @GetMapping("/permanent-address")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<PermanentAddressResponse>> getMyPermanentAddress() {

        PermanentAddressResponse response =
                candidateContactDetailsService.getMyPermanentAddress();

        return ResponseEntity.ok(
                ApiResponse.success("Permanent address fetched successfully.", response)
        );
    }

    // =========================================================
    // CURRENT ADDRESS
    // =========================================================

    @PostMapping("/current-address")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<CurrentAddressResponse>> createCurrentAddress(
            @Valid @RequestBody CurrentAddressRequest request) {

        CurrentAddressResponse response =
                candidateContactDetailsService.createOrUpdateCurrentAddress(request);

        return ResponseEntity.status(201).body(
                ApiResponse.success("Current address saved successfully.", response)
        );
    }

    @PutMapping("/current-address")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<CurrentAddressResponse>> updateCurrentAddress(
            @Valid @RequestBody CurrentAddressRequest request) {

        CurrentAddressResponse response =
                candidateContactDetailsService.createOrUpdateCurrentAddress(request);

        return ResponseEntity.ok(
                ApiResponse.success("Current address updated successfully.", response)
        );
    }

    @GetMapping("/current-address")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<CurrentAddressResponse>> getMyCurrentAddress() {

        CurrentAddressResponse response =
                candidateContactDetailsService.getMyCurrentAddress();

        return ResponseEntity.ok(
                ApiResponse.success("Current address fetched successfully.", response)
        );
    }
    // =========================================================
// HR / ADMIN
// =========================================================

    @GetMapping("/candidate/{candidateId}/permanent-address")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<ApiResponse<PermanentAddressResponse>> getPermanentAddressByCandidateId(
            @PathVariable Long candidateId) {

        PermanentAddressResponse response =
                candidateContactDetailsService.getPermanentAddressByCandidateId(candidateId);

        return ResponseEntity.ok(
                ApiResponse.success("Candidate permanent address fetched successfully.", response)
        );
    }

    @GetMapping("/candidate/{candidateId}/current-address")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<ApiResponse<CurrentAddressResponse>> getCurrentAddressByCandidateId(
            @PathVariable Long candidateId) {

        CurrentAddressResponse response =
                candidateContactDetailsService.getCurrentAddressByCandidateId(candidateId);

        return ResponseEntity.ok(
                ApiResponse.success("Candidate current address fetched successfully.", response)
        );
    }
}