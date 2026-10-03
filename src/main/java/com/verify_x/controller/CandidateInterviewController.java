package com.verify_x.controller;

import com.verify_x.dto.InterviewSlotResponseDto;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.InterviewSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateInterviewController {

    private final InterviewSlotService interviewSlotService;

    @GetMapping("/interview-slots")
    public ResponseEntity<ApiResponse<List<InterviewSlotResponseDto>>> getInterviewSlots(
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.<List<InterviewSlotResponseDto>>builder()
                        .success(true)
                        .message("Interview slots fetched successfully.")
                        .data(interviewSlotService.getCandidateSlots(principal.getUserId()))
                        .build()
        );
    }

    @PostMapping("/interview-slots/{slotId}/select")
    public ResponseEntity<ApiResponse<String>> selectInterviewSlot(
            @PathVariable Long slotId,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        interviewSlotService.selectInterviewSlot(principal.getUserId(), slotId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Interview slot selected successfully.")
                        .data("Success")
                        .build()
        );
    }
}
