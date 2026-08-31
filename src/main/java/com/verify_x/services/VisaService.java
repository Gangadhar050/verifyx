//package com.verify_x.services;
//
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.VisaUploadRequest;
//import com.verify_x.entity.CandidateDocument;
//import org.springframework.core.io.Resource;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//public interface VisaService {
//
//    // Candidate uploads their visa. Mandatory - throws if missing.
//    void uploadVisa(VisaUploadRequest request);
//
//    // Get logged-in candidate's visa.
//    List<CandidateDocumentDto> getMyVisa();
//
//    // HR/Admin - fetch a candidate's visa by candidate id.
//    List<CandidateDocumentDto> getVisaByCandidateId(Long candidateId);
//
//    // Fetch raw document (for view/download), scoped to VISA only.
//    CandidateDocument getVisaDocument(Long documentId);
//
//    // Download visa file bytes.
//    Resource downloadVisa(Long documentId);
//
//    // HR verifies the visa.
//    void verifyVisa(Long documentId);
//
//    // HR rejects the visa with a reason.
//    void rejectVisa(Long documentId, String rejectionReason);
//
//    // HR - all pending visas awaiting review.
//    List<CandidateDocumentDto> getPendingVisas();
//
//    // Candidate can update their own visa.
//    void updateVisa(Long documentId, MultipartFile visa);
//
//    // Candidate can delete their own visa.
//    void deleteVisa(Long documentId);
//}