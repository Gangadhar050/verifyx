package com.verify_x.controller;

import com.verify_x.dto.HrOfferLetterRequestDTO;
import com.verify_x.dto.HrOfferLetterResponseDTO;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.HrOfferLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hr/candidates/{candidateId}/offer-letters")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR','ADMIN')")
public class HrOfferLetterController {

    private final HrOfferLetterService hrOfferLetterService;

    // POST /api/hr/candidates/{candidateId}/offer-letters/send
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<HrOfferLetterResponseDTO>> send(
            @PathVariable Long candidateId,
            @Valid @RequestBody HrOfferLetterRequestDTO request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        HrOfferLetterResponseDTO data = hrOfferLetterService.sendOfferLetter(
                candidateId, request, principal.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<HrOfferLetterResponseDTO>builder()
                        .success(true)
                        .message("Offer letter sent to " + data.getCandidateEmail())
                        .data(data)
                        .timeStamp(LocalDateTime.now())
                        .build());
    }

    // GET /api/hr/candidates/{candidateId}/offer-letters
    @GetMapping
    public ResponseEntity<ApiResponse<List<HrOfferLetterResponseDTO>>> list(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(
                ApiResponse.<List<HrOfferLetterResponseDTO>>builder()
                        .success(true)
                        .message("Offer letters fetched successfully.")
                        .data(hrOfferLetterService.getSentOfferLetters(candidateId))
                        .timeStamp(LocalDateTime.now())
                        .build());
    }
}