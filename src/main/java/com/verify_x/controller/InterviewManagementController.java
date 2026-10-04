package com.verify_x.controller;

import com.verify_x.dto.InterviewCandidateDto;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/interviews")
@RequiredArgsConstructor
public class InterviewManagementController {

    private final InterviewService interviewService;

    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<InterviewCandidateDto>>>
    getInterviewCandidates() {

        return ResponseEntity.ok(
                ApiResponse.<List<InterviewCandidateDto>>builder()
                        .success(true)
                        .message(
                                "Interview candidates fetched successfully.")
                        .data(
                                interviewService
                                        .getInterviewCandidates())
                        .build()
        );
    }

    @PutMapping("/{candidateId}/confirm")
    public ResponseEntity<ApiResponse<String>>
    confirmInterview(
            @PathVariable Long candidateId,
            Authentication authentication) {

        interviewService.confirmInterview(
                candidateId,
                authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(
                                "Interview confirmed successfully.")
                        .data("Success")
                        .build()
        );
    }

    @PutMapping("/{candidateId}/approve")
    public ResponseEntity<ApiResponse<String>>
    approveInterview(
            @PathVariable Long candidateId,
            @RequestParam(required = false) String remarks) {

        interviewService.approveInterview(
                candidateId,
                remarks);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(
                                "Interview approved successfully.")
                        .data("Success")
                        .build()
        );
    }

    @PutMapping("/{candidateId}/reject")
    public ResponseEntity<ApiResponse<String>>
    rejectInterview(
            @PathVariable Long candidateId,
            @RequestParam(required = false) String remarks) {

        interviewService.rejectInterview(
                candidateId,
                remarks);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(
                                "Interview rejected successfully.")
                        .data("Success")
                        .build()
        );
    }
    @GetMapping("/approved")
    public ResponseEntity<List<InterviewCandidateDto>>
    getApprovedInterviewCandidates() {

        return ResponseEntity.ok(
                interviewService.getApprovedInterviewCandidates()
        );
    }

}