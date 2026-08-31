//package com.verify_x.controller;
//
//import com.verify_x.dto.CandidateContactDetailsRequest1;
//import com.verify_x.dto.CandidateContactDetailsResponse1;
//import com.verify_x.payload.ApiResponse;
//import com.verify_x.services.CandidateContactDetailsServices;
//
//import jakarta.validation.Valid;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/candidate/contact-details")
//@RequiredArgsConstructor
//public class CandidateContactDetailsControllers {
//
//    private final CandidateContactDetailsServices
//            candidateContactDetailsService;
//
//
//    // =========================================================
//    // CREATE
//    // =========================================================
//
//    @PostMapping
//    @PreAuthorize("hasRole('CANDIDATE')")
//    public ResponseEntity<
//            ApiResponse<CandidateContactDetailsResponse1>>
//    createDetails(
//            @Valid
//            @RequestBody
//            CandidateContactDetailsRequest1 request) {
//
//        CandidateContactDetailsResponse1 response =
//                candidateContactDetailsService
//                        .createDetails(request);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(
//                        ApiResponse.success(
//                                "Contact and address details saved successfully.",
//                                response
//                        )
//                );
//    }
//
//
//    // =========================================================
//    // UPDATE
//    // =========================================================
//
//    @PutMapping
//    @PreAuthorize("hasRole('CANDIDATE')")
//    public ResponseEntity<
//            ApiResponse<CandidateContactDetailsResponse1>>
//    updateDetails(
//            @Valid
//            @RequestBody
//            CandidateContactDetailsRequest1 request) {
//
//        CandidateContactDetailsResponse1 response =
//                candidateContactDetailsService
//                        .updateDetails(request);
//
//        return ResponseEntity.ok(
//                ApiResponse.success(
//                        "Contact and address details updated successfully.",
//                        response
//                )
//        );
//    }
//
//
//    // =========================================================
//    // GET MY DETAILS
//    // =========================================================
//
//    @GetMapping("/me")
//    @PreAuthorize("hasRole('CANDIDATE')")
//    public ResponseEntity<
//            ApiResponse<CandidateContactDetailsResponse1>>
//    getMyDetails() {
//
//        CandidateContactDetailsResponse1 response =
//                candidateContactDetailsService
//                        .getMyDetails();
//
//        return ResponseEntity.ok(
//                ApiResponse.success(
//                        "Contact and address details fetched successfully.",
//                        response
//                )
//        );
//    }
//
//
//    // =========================================================
//    // HR / ADMIN
//    // GET DETAILS BY CANDIDATE ID
//    // =========================================================
//
//    @GetMapping("/candidate/{candidateId}")
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    public ResponseEntity<
//            ApiResponse<CandidateContactDetailsResponse1>>
//    getDetailsByCandidateId(
//            @PathVariable Long candidateId) {
//
//        CandidateContactDetailsResponse1 response =
//                candidateContactDetailsService
//                        .getDetailsByCandidateId(candidateId);
//
//        return ResponseEntity.ok(
//                ApiResponse.success(
//                        "Candidate contact and address details fetched successfully.",
//                        response
//                )
//        );
//    }
//}