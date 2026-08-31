//package com.verify_x.controller;
//
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.PassportUploadRequest;
//import com.verify_x.entity.CandidateDocument;
//import com.verify_x.payload.ApiResponse;
//import com.verify_x.services.PassportService;
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
//@RequestMapping("/api/passport")
//@RequiredArgsConstructor
//public class PassportController {
//
//    private final PassportService passportService;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasRole('CANDIDATE')")
//    public ResponseEntity<ApiResponse<String>> uploadPassport(
//            @ModelAttribute PassportUploadRequest request) {
//
//        passportService.uploadPassport(request);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("Passport uploaded successfully.", null));
//    }
//    // =========================================================
//// UPDATE PASSPORT
//// =========================================================
//
//    @PreAuthorize("hasRole('CANDIDATE')")
//    @PutMapping(
//            value = "/update/{documentId}",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ResponseEntity<ApiResponse<String>> updatePassport(
//            @PathVariable Long documentId,
//            @RequestPart("passport") MultipartFile passport) {
//
//        passportService.updatePassport(documentId, passport);
//        return ResponseEntity.ok(ApiResponse.success(
//                        "Passport updated successfully.",
//                        null));
//    }
//
//    @PreAuthorize("hasRole('CANDIDATE')")
//    @GetMapping("/my-passport")
//    public ResponseEntity<List<CandidateDocumentDto>> getMyPassport() {
//        return ResponseEntity.ok(passportService.getMyPassport());
//    }
//
//    @PreAuthorize("hasAnyRole('HR','CANDIDATE','ADMIN')")
//    @GetMapping("/candidate/{candidateId}")
//    public ResponseEntity<List<CandidateDocumentDto>> getPassportByCandidateId(
//            @PathVariable Long candidateId) {
//        return ResponseEntity.ok(passportService.getPassportByCandidateId(candidateId));
//    }
//
//    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
//    @GetMapping("/view/{documentId}")
//    public ResponseEntity<byte[]> viewPassport(@PathVariable Long documentId) {
//
//        CandidateDocument document = passportService.getPassportDocument(documentId);
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
//    public ResponseEntity<Resource> downloadPassport(@PathVariable Long documentId) {
//
//        CandidateDocument document = passportService.getPassportDocument(documentId);
//        Resource resource = passportService.downloadPassport(documentId);
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
//    public ResponseEntity<ApiResponse<String>> verifyPassport(@PathVariable Long documentId) {
//
//        passportService.verifyPassport(documentId);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("Passport verified successfully.", null));
//    }
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @PutMapping("/reject/{documentId}")
//    public ResponseEntity<ApiResponse<String>> rejectPassport(
//            @PathVariable Long documentId,
//            @RequestParam String reason) {
//
//        passportService.rejectPassport(documentId, reason);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("Passport rejected successfully.", null));
//    }
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @GetMapping("/pending")
//    public ResponseEntity<List<CandidateDocumentDto>> getPendingPassports() {
//        return ResponseEntity.ok(passportService.getPendingPassports());
//    }
//
//    @PreAuthorize("hasRole('CANDIDATE')")
//    @DeleteMapping("/delete/{documentId}")
//    public ResponseEntity<ApiResponse<String>> deletePassport(
//            @PathVariable Long documentId) {
//        passportService.deletePassport(documentId);
//        return ResponseEntity.ok(ApiResponse.success("Passport deleted successfully.",
//                        null));
//    }
//}