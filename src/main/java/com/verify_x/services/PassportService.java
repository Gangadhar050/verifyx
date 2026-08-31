//package com.verify_x.services;
//
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.PassportUploadRequest;
//import com.verify_x.entity.CandidateDocument;
//import org.springframework.core.io.Resource;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//public interface PassportService {
//
//    // Candidate uploads their passport. Mandatory - throws if missing.
//    void uploadPassport(PassportUploadRequest request);
//
//    // Get logged-in candidate's passport.
//    List<CandidateDocumentDto> getMyPassport();
//
//    // HR/Admin - fetch a candidate's passport by candidate id.
//    List<CandidateDocumentDto> getPassportByCandidateId(Long candidateId);
//
//    // Fetch raw document (for view/download), scoped to PASSPORT only.
//    CandidateDocument getPassportDocument(Long documentId);
//
//    // Download passport file bytes.
//    Resource downloadPassport(Long documentId);
//
//    // HR verifies the passport.
//    void verifyPassport(Long documentId);
//
//    // HR rejects the passport with a reason.
//    void rejectPassport(Long documentId, String rejectionReason);
//
//    // HR - all pending passports awaiting review.
//    List<CandidateDocumentDto> getPendingPassports();
//    void updatePassport(Long documentId, MultipartFile passport);
//
//    void deletePassport(Long documentId);
//}