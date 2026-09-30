package com.verify_x.controller;

import com.verify_x.dto.*;
import com.verify_x.enums.VerificationStatus;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateManagementService;
import com.verify_x.services.InterviewSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import org.springframework.web.bind.annotation.RequestParam;



import java.util.List;

@RestController
@RequestMapping("/api/hr/candidates")
@RequiredArgsConstructor
public class CandidateManagementController {

    private final CandidateManagementService candidateManagementService;
    private final InterviewSlotService interviewSlotService;
@GetMapping
public ResponseEntity<List<CandidateSummaryDto>> getAllCandidates() {

    return ResponseEntity.ok(
            candidateManagementService.getAllCandidates()
    );
}
    @GetMapping("/search")
    public ResponseEntity<List<CandidateSummaryDto>> searchCandidates(

            @RequestParam String keyword
    ) {

        return ResponseEntity.ok(
                candidateManagementService.searchCandidates(keyword)
        );
    }
    @GetMapping("/{candidateId}")
    public ResponseEntity<CandidateDetailsDto> getCandidateDetails(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(
                candidateManagementService.getCandidateDetails(candidateId));
    }


    @DeleteMapping("/{candidateId}")
    public ResponseEntity<String> deleteCandidate(
            @PathVariable Long candidateId) {

        candidateManagementService.deleteCandidate(candidateId);
        return ResponseEntity.ok("Candidate deleted successfully.");
    }
    @PutMapping("/{candidateId}/verify-uan")
    public ResponseEntity<String> verifyUan(
            @PathVariable Long candidateId,
            @RequestParam VerificationStatus status,
            Authentication authentication) {

        candidateManagementService.verifyUan(
                candidateId,
                status,
                authentication.getName()
        );

        return ResponseEntity.ok("UAN status updated successfully.");
    }
//    @PutMapping("/{candidateId}/verify-uan")
//    public ResponseEntity<String> verifyUan(
//
//            @PathVariable Long candidateId,
//
//            @RequestBody UanVerificationRequestDto request,
//
//            Authentication authentication) {
//
//        candidateManagementService.verifyUan(
//
//                candidateId,
//
//                request.getStatus(),
//
//                authentication.getName());
//
//        return ResponseEntity.ok("UAN status updated successfully.");
//    }

    @PutMapping("/{candidateId}/status")
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable Long candidateId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String remarks,

            Authentication authentication) {

        ApplicationStatusUpdateDto dto = new ApplicationStatusUpdateDto();
        dto.setStatus(status);
        dto.setRemarks(remarks);

        candidateManagementService.updateApplicationStatus(
                candidateId,
                dto,
                authentication.getName()
        );

        return ResponseEntity.ok("Application status updated successfully.");
    }
    @PostMapping("/{candidateId}/interview-slots")
    public ResponseEntity<ApiResponse<String>> sendInterviewSlots(
            @PathVariable Long candidateId,
            @Valid @RequestBody InterviewSlotRequestDto request) {

        interviewSlotService.createAndSendInterviewSlots(
                candidateId,
                request.getSlots()
        );

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(
                                "Interview slots sent successfully.")
                        .data("Success")
                        .build()
        );
    }

}
