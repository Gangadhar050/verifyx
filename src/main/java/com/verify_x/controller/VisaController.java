//package com.verify_x.controller;
//
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.VisaUploadRequest;
//import com.verify_x.entity.CandidateDocument;
//import com.verify_x.payload.ApiResponse;
//import com.verify_x.services.VisaService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/visa")
//@RequiredArgsConstructor
//public class VisaController {
//
//    private final VisaService visaService;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasRole('CANDIDATE')")
//    public ResponseEntity<ApiResponse<String>> uploadVisa(
//            @ModelAttribute VisaUploadRequest request) {
//
//        visaService.uploadVisa(request);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("Visa uploaded successfully.", null));
//    }
//    // =========================================================
//// UPDATE VISA
//// =========================================================
//
//    @PreAuthorize("hasRole('CANDIDATE')")
//    @PutMapping(
//            value = "/update/{documentId}",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ResponseEntity<ApiResponse<String>> updateVisa(
//            @PathVariable Long documentId,
//            @RequestPart("visa") MultipartFile visa) {
//
//        visaService.updateVisa(
//                documentId,
//                visa);
//
//        return ResponseEntity.ok(
//                ApiResponse.success(
//                        "Visa updated successfully.",
//                        null));
//    }
//
//    @PreAuthorize("hasRole('CANDIDATE')")
//    @GetMapping("/my-visa")
//    public ResponseEntity<List<CandidateDocumentDto>> getMyVisa() {
//        return ResponseEntity.ok(visaService.getMyVisa());
//    }
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @GetMapping("/candidate/{candidateId}")
//    public ResponseEntity<List<CandidateDocumentDto>> getVisaByCandidateId(
//            @PathVariable Long candidateId) {
//        return ResponseEntity.ok(visaService.getVisaByCandidateId(candidateId));
//    }
//
//    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
//    @GetMapping("/view/{documentId}")
//    public ResponseEntity<byte[]> viewVisa(@PathVariable Long documentId) {
//
//        CandidateDocument document = visaService.getVisaDocument(documentId);
//
//        MediaType mediaType;
//        try {
//            mediaType = MediaType.parseMediaType(document.getContentType());
//        } catch (Exception ex) {
//            mediaType = MediaType.APPLICATION_OCTET_STREAM;
//        }
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + document.getFileName().replace("\"", "") + "\"")
//                .contentType(mediaType)
//                .body(document.getDocumentData());
//    }
//
//    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
//    @GetMapping("/download/{documentId}")
//    public ResponseEntity<Resource> downloadVisa(@PathVariable Long documentId) {
//
//        CandidateDocument document = visaService.getVisaDocument(documentId);
//        Resource resource = visaService.downloadVisa(documentId);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + document.getFileName().replace("\"", "") + "\"")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
//    }
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @PutMapping("/verify/{documentId}")
//    public ResponseEntity<ApiResponse<String>> verifyVisa(@PathVariable Long documentId) {
//
//        visaService.verifyVisa(documentId);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("Visa verified successfully.", null));
//    }
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @PutMapping("/reject/{documentId}")
//    public ResponseEntity<ApiResponse<String>> rejectVisa(
//            @PathVariable Long documentId,
//            @RequestParam String reason) {
//
//        visaService.rejectVisa(documentId, reason);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("Visa rejected successfully.", null));
//    }
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @GetMapping("/pending")
//    public ResponseEntity<List<CandidateDocumentDto>> getPendingVisas() {
//        return ResponseEntity.ok(visaService.getPendingVisas());
//    }
//    // =========================================================
//// DELETE VISA
//// =========================================================
//
//    @PreAuthorize("hasRole('CANDIDATE')")
//    @DeleteMapping("/delete/{documentId}")
//    public ResponseEntity<ApiResponse<String>> deleteVisa(
//            @PathVariable Long documentId) {
//
//        visaService.deleteVisa(documentId);
//
//        return ResponseEntity.ok(ApiResponse.success(
//                        "Visa deleted successfully.",
//                        null));
//    }
//}