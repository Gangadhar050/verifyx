//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.VisaUploadRequest;
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.CandidateDocument;
//import com.verify_x.enums.DocumentStatus;
//import com.verify_x.enums.DocumentType;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.exception.ResourceNotFoundException;
//import com.verify_x.jwt.UserPrincipal;
//import com.verify_x.repository.CandidateDocumentRepository;
//import com.verify_x.repository.CandidateRepository;
//import com.verify_x.services.VisaService;
//
//import jakarta.transaction.Transactional;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class VisaServiceImpl implements VisaService {
//
//    private static final long MAX_FILE_SIZE =
//            5 * 1024 * 1024;
//
//    private static final List<String> ALLOWED_CONTENT_TYPES =
//            List.of(
//                    "application/pdf",
//                    "image/jpeg",
//                    "image/png");
//
//    private final CandidateRepository candidateRepository;
//
//    private final CandidateDocumentRepository
//            candidateDocumentRepository;
//
//    @Override
//    public void uploadVisa(
//            VisaUploadRequest request) {
//
//        /*
//         * Visa is mandatory.
//         * Reject the request outright if it is missing.
//         */
//        if (request == null ||
//                request.getVisa() == null ||
//                request.getVisa().isEmpty()) {
//
//            throw new BadRequestException("Visa document is required.");
//        }
//
//        Candidate candidate = getLoggedInCandidate();
//
//        MultipartFile visa = request.getVisa();
//
//        validateVisa(visa);
//
//        /*
//         * Find existing Visa belonging
//         * to this candidate.
//         */
//        CandidateDocument existingVisa =
//                candidateDocumentRepository.findByCandidateAndDocumentType(
//                                candidate,
//                                DocumentType.VISA)
//                        .orElse(null);
//        /*
//         * Do not overwrite a verified document.
//         */
//        if (existingVisa != null &&
//                existingVisa.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException(
//                    "Visa is already verified and cannot be replaced.");
//        }
//
//        try {
//            CandidateDocument visaDocument;
//
//            if (existingVisa == null) {
//                /*
//                 * First Visa upload.
//                 */
//                visaDocument = CandidateDocument.builder().candidate(candidate).documentType(DocumentType.VISA)
//                                .build();
//            } else {
//                /*
//                 * Existing Visa was rejected.
//                 * Replace its document data.
//                 */
//                visaDocument =
//                        existingVisa;
//            }
//
//            visaDocument.setFileName(
//                    sanitizeFileName(visa.getOriginalFilename()));
//
//            visaDocument.setContentType(visa.getContentType());
//
//            visaDocument.setDocumentData(visa.getBytes());
//
//            /*
//             * Every new upload must go through
//             * the verification process again.
//             */
//            visaDocument.setStatus(DocumentStatus.PENDING);
//            /*
//             * Clear previous rejection information.
//             */
//            visaDocument.setRejectionReason(null);
//
//            candidateDocumentRepository.save(visaDocument);
//
//            log.info(
//                    "Visa uploaded successfully. " +
//                            "candidateId={}, documentId={}",
//                    candidate.getId(), visaDocument.getId());
//
//        } catch (IOException exception) {
//
//            log.error("Failed to upload visa. candidateId={}",
//                    candidate.getId(), exception);
//
//            throw new BadRequestException("Unable to upload visa.");
//        }
//    }
//
//    // =========================================================
//    // GET MY VISA
//    // =========================================================
//
//    @Override
//    @Transactional
//    public List<CandidateDocumentDto> getMyVisa() {
//
//        Candidate candidate =
//                getLoggedInCandidate();
//
//        return candidateDocumentRepository.findByCandidateAndDocumentType(candidate, DocumentType.VISA)
//                .map(this::mapToDto)
//                .stream()
//                .toList();
//    }
//
//    // =========================================================
//    // GET VISA BY CANDIDATE ID (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    @Transactional
//    public List<CandidateDocumentDto> getVisaByCandidateId(
//            Long candidateId) {
//
//        Candidate candidate = candidateRepository.findById(candidateId).orElseThrow(() ->
//                                new ResourceNotFoundException("Candidate", candidateId));
//
//        return candidateDocumentRepository.findByCandidateAndDocumentType(candidate, DocumentType.VISA)
//                .map(this::mapToDto)
//                .stream()
//                .toList();
//    }
//
//    // =========================================================
//    // GET RAW VISA DOCUMENT (scoped to VISA only)
//    // =========================================================
//
//    @Override
//    public CandidateDocument getVisaDocument(
//            Long documentId) {
//
//        return findOwnedDocumentOrThrow(documentId);
//    }
//
//    // =========================================================
//    // DOWNLOAD VISA
//    // =========================================================
//
//    @Override
//    public Resource downloadVisa(
//            Long documentId) {
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        UserPrincipal principal = getAuthenticatedPrincipal();
//        /*
//         * Candidates may only download their own visa.
//         */
//        if ("CANDIDATE".equals(principal.getRole().name()) &&
//                !document.getCandidate()
//                        .getId()
//                        .equals(principal.getUserId())) {
//
//            throw new BadRequestException("You are not authorized to download this document.");
//        }
//
//        return new ByteArrayResource(document.getDocumentData());
//    }
//
//    // =========================================================
//    // VERIFY VISA (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    public void verifyVisa(Long documentId) {
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Visa is already verified.");
//        }
//
//        document.setStatus(DocumentStatus.VERIFIED);
//
//        document.setRejectionReason(null);
//
//        candidateDocumentRepository.save(document);
//
//        log.info("Visa verified. documentId={}, candidateId={}",
//                documentId, document.getCandidate().getId());
//    }
//
//    // =========================================================
//    // REJECT VISA (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    public void rejectVisa(Long documentId, String rejectionReason) {
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Verified visa cannot be rejected.");
//        }
//
//        if (rejectionReason == null || rejectionReason.isBlank()) {
//
//            throw new BadRequestException("Rejection reason is required.");
//        }
//
//        document.setStatus(DocumentStatus.REJECTED);
//
//        document.setRejectionReason(rejectionReason.trim());
//
//        candidateDocumentRepository.save(document);
//
//        log.info(
//                "Visa rejected. documentId={}, candidateId={}, reason={}",
//                documentId,
//                document.getCandidate().getId(),
//                rejectionReason);
//    }
//
//    // =========================================================
//    // PENDING VISAS (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    public List<CandidateDocumentDto> getPendingVisas() {
//
//        return candidateDocumentRepository
//                .findByStatus(DocumentStatus.PENDING)
//                .stream()
//                .filter(document ->
//                        document.getDocumentType()
//                                == DocumentType.VISA)
//                .map(this::mapToDto)
//                .toList();
//    }
//
//    // =========================================================
//    // VALIDATE VISA
//    // =========================================================
//
//    private void validateVisa(
//            MultipartFile visa) {
//
//        if (visa.isEmpty()) {
//
//            throw new BadRequestException("Visa file cannot be empty.");
//        }
//
//        if (visa.getSize() > MAX_FILE_SIZE) {
//
//            throw new BadRequestException("Visa file size cannot exceed 5 MB.");
//        }
//
//        String contentType =
//                visa.getContentType();
//
//        if (contentType == null ||
//                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
//
//            throw new BadRequestException(
//                    "Invalid visa file type. " + "Only PDF, JPG, JPEG and PNG files are allowed.");
//        }
//    }
//
//    // =========================================================
//    // OWNERSHIP GUARD - ensures this service only ever
//    // touches documents of type VISA, even though it
//    // shares the candidate_documents table with other types.
//    // =========================================================
//
//    private CandidateDocument findOwnedDocumentOrThrow(
//            Long documentId) {
//
//        CandidateDocument document = candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException("Document", documentId));
//
//        if (document.getDocumentType() != DocumentType.VISA) {
//
//            throw new BadRequestException("Document " + documentId + " is not a visa document.");
//        }
//
//        return document;
//    }
//
//    // =========================================================
//    // GET LOGGED-IN CANDIDATE
//    // =========================================================
//
//    private Candidate getLoggedInCandidate() {
//
//        UserPrincipal principal = getAuthenticatedPrincipal();
//
//        Long candidateId = principal.getUserId();
//
//        if (candidateId == null) {
//
//            throw new BadRequestException("Candidate information is missing from authentication.");
//        }
//
//        return candidateRepository
//                .findById(candidateId)
//                .orElseThrow(() -> new BadRequestException("Candidate not found."));
//    }
//
//    // =========================================================
//    // GET AUTHENTICATED PRINCIPAL
//    // =========================================================
//
//    private UserPrincipal getAuthenticatedPrincipal() {
//
//        Authentication authentication =
//                SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//
//        if (authentication == null ||
//                authentication.getPrincipal() == null) {
//
//            throw new BadRequestException("User is not authenticated.");
//        }
//
//        if (!(authentication.getPrincipal()
//                instanceof UserPrincipal)) {
//
//            throw new BadRequestException("Invalid authentication details.");
//        }
//
//        return (UserPrincipal)
//                authentication.getPrincipal();
//    }
//
//    // =========================================================
//    // FILE NAME SANITIZATION
//    // =========================================================
//
//    private String sanitizeFileName(
//            String originalFileName) {
//
//        if (originalFileName == null || originalFileName.isBlank()) {
//
//            return "visa";
//        }
//
//        String fileName = originalFileName.replace("\\", "/");
//
//        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
//        /*
//         * Prevent path traversal and unsafe
//         * characters in the stored file name.
//         */
//        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
//
//        if (fileName.length() > 255) {
//
//            fileName = fileName.substring(fileName.length() - 255);
//        }
//
//        return fileName;
//    }
//
//    // =========================================================
//    // DTO MAPPING
//    // =========================================================
//
//    private CandidateDocumentDto mapToDto(
//            CandidateDocument document) {
//
//        return CandidateDocumentDto.builder()
//                .id(document.getId())
//                .documentType(document.getDocumentType())
//                .fileName(document.getFileName())
//                .contentType(document.getContentType())
//                .status(document.getStatus()).rejectionReason(
//                        document.getRejectionReason()).uploadedAt(
//                        document.getUploadedAt())
//                .updatedAt(document.getUpdatedAt())
//                .build();
//    }
//    // =========================================================
//// UPDATE VISA
//// =========================================================
//
//    @Override
//    public void updateVisa(
//            Long documentId,
//            MultipartFile visa) {
//
//        if (visa == null || visa.isEmpty()) {
//
//            throw new BadRequestException(
//                    "Visa document is required.");
//        }
//
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateDocument document =
//                findOwnedDocumentOrThrow(documentId);
//
//        /*
//         * Candidate can update only their own visa.
//         */
//        if (!document.getCandidate()
//                .getId()
//                .equals(candidate.getId())) {
//
//            throw new BadRequestException(
//                    "You are not authorized to update this visa.");
//        }
//
//        /*
//         * Verified visa cannot be replaced.
//         */
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException(
//                    "Verified visa cannot be updated.");
//        }
//
//        validateVisa(visa);
//
//        try {
//
//            document.setFileName(
//                    sanitizeFileName(
//                            visa.getOriginalFilename()));
//
//            document.setContentType(
//                    visa.getContentType());
//
//            document.setDocumentData(
//                    visa.getBytes());
//
//            /*
//             * Updated visa must go through verification again.
//             */
//            document.setStatus(
//                    DocumentStatus.PENDING);
//
//            /*
//             * Clear previous rejection reason.
//             */
//            document.setRejectionReason(null);
//
//            candidateDocumentRepository.save(document);
//
//            log.info(
//                    "Visa updated successfully. candidateId={}, documentId={}",
//                    candidate.getId(),
//                    documentId);
//
//        } catch (IOException exception) {
//
//            log.error(
//                    "Failed to update visa. candidateId={}, documentId={}",
//                    candidate.getId(),
//                    documentId,
//                    exception);
//
//            throw new BadRequestException(
//                    "Unable to update visa.");
//        }
//    }
//    // =========================================================
//// DELETE VISA
//// =========================================================
//
//    @Override
//    public void deleteVisa(Long documentId) {
//
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateDocument document =
//                findOwnedDocumentOrThrow(documentId);
//
//        /*
//         * Candidate can delete only their own visa.
//         */
//        if (!document.getCandidate()
//                .getId()
//                .equals(candidate.getId())) {
//
//            throw new BadRequestException(
//                    "You are not authorized to delete this visa.");
//        }
//
//        /*
//         * Verified visa cannot be deleted.
//         */
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException(
//                    "Verified visa cannot be deleted.");
//        }
//
//        candidateDocumentRepository.delete(document);
//
//        log.info(
//                "Visa deleted successfully. candidateId={}, documentId={}",
//                candidate.getId(),
//                documentId);
//    }
//
//}