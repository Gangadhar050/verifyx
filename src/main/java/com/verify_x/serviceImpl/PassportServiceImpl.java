//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.PassportUploadRequest;
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.CandidateDocument;
//import com.verify_x.enums.DocumentStatus;
//import com.verify_x.enums.DocumentType;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.exception.ResourceNotFoundException;
//import com.verify_x.jwt.UserPrincipal;
//import com.verify_x.repository.CandidateDocumentRepository;
//import com.verify_x.repository.CandidateRepository;
//import com.verify_x.services.PassportService;
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
//public class PassportServiceImpl implements PassportService {
//
//    private static final long MAX_FILE_SIZE =
//            5 * 1024 * 1024;
//
//    private static final List<String> ALLOWED_CONTENT_TYPES =
//            List.of("application/pdf",
//                    "image/jpeg",
//                    "image/png");
//
//    private final CandidateRepository candidateRepository;
//
//    private final CandidateDocumentRepository
//            candidateDocumentRepository;
//
//    // =========================================================
//    // UPLOAD PASSPORT
//    // =========================================================
//
//    @Override
//    public void uploadPassport(
//            PassportUploadRequest request) {
//
//        /*
//         * Passport is mandatory.
//         * Reject the request outright if it is missing.
//         */
//        if (request == null ||
//                request.getPassport() == null ||
//                request.getPassport().isEmpty()) {
//
//            throw new BadRequestException("Passport document is required.");
//        }
//
//        Candidate candidate = getLoggedInCandidate();
//
//        MultipartFile passport = request.getPassport();
//
//        validatePassport(passport);
//
//        /*
//         * Find existing Passport belonging
//         * to this candidate.
//         */
//        CandidateDocument existingPassport = candidateDocumentRepository.findByCandidateAndDocumentType(
//                                candidate,
//                                DocumentType.PASSPORT)
//                        .orElse(null);
//        /*
//         * Do not overwrite a verified document.
//         */
//        if (existingPassport != null && existingPassport.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Passport is already verified and cannot be replaced.");
//        }
//
//        try {
//
//            CandidateDocument passportDocument;
//
//            if (existingPassport == null) {
//                /*
//                 * First Passport upload.
//                 */
//                passportDocument = CandidateDocument.builder()
//                                .candidate(candidate)
//                                .documentType(DocumentType.PASSPORT)
//                                .build();
//
//            } else {
//                /*
//                 * Existing Passport was rejected.
//                 * Replace its document data.
//                 */
//                passportDocument = existingPassport;
//            }
//
//            passportDocument.setFileName(sanitizeFileName(passport.getOriginalFilename()));
//
//            passportDocument.setContentType(passport.getContentType());
//
//            passportDocument.setDocumentData(passport.getBytes());
//
//            /*
//             * Every new upload must go through
//             * the verification process again.
//             */
//            passportDocument.setStatus(DocumentStatus.PENDING);
//            /*
//             * Clear previous rejection information.
//             */
//            passportDocument.setRejectionReason(null);
//
//            candidateDocumentRepository.save(passportDocument);
//
//            log.info("Passport uploaded successfully. " +
//                            "candidateId={}, documentId={}",
//                    candidate.getId(),
//                    passportDocument.getId());
//
//        } catch (IOException exception) {
//
//            log.error("Failed to upload passport. candidateId={}",
//                    candidate.getId(),
//                    exception);
//
//            throw new BadRequestException("Unable to upload passport.");
//        }
//    }
//
//    // =========================================================
//    // GET MY PASSPORT
//    // =========================================================
//
//    @Override
//    @Transactional
//    public List<CandidateDocumentDto> getMyPassport() {
//
//        Candidate candidate = getLoggedInCandidate();
//
//        return candidateDocumentRepository.findByCandidateAndDocumentType(
//                        candidate,
//                        DocumentType.PASSPORT)
//                .map(this::mapToDto)
//                .stream()
//                .toList();
//    }
//
//    // =========================================================
//    // GET PASSPORT BY CANDIDATE ID (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    @Transactional
//    public List<CandidateDocumentDto> getPassportByCandidateId(
//            Long candidateId) {
//
//        Candidate candidate = candidateRepository.findById(candidateId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException("Candidate", candidateId));
//
//        return candidateDocumentRepository.findByCandidateAndDocumentType(
//                        candidate,
//                        DocumentType.PASSPORT)
//                .map(this::mapToDto)
//                .stream()
//                .toList();
//    }
//
//    // =========================================================
//    // GET RAW PASSPORT DOCUMENT (scoped to PASSPORT only)
//    // =========================================================
//
//    @Override
//    public CandidateDocument getPassportDocument(Long documentId) {
//
//        return findOwnedDocumentOrThrow(documentId);
//    }
//
//    // =========================================================
//    // DOWNLOAD PASSPORT
//    // =========================================================
//
//    @Override
//    public Resource downloadPassport(Long documentId) {
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        UserPrincipal principal = getAuthenticatedPrincipal();
//
//        /*
//         * Candidates may only download their own passport.
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
//    // VERIFY PASSPORT (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    public void verifyPassport(
//            Long documentId) {
//
//        CandidateDocument document =
//                findOwnedDocumentOrThrow(documentId);
//
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Passport is already verified.");
//        }
//
//        document.setStatus(DocumentStatus.VERIFIED);
//
//        document.setRejectionReason(null);
//
//        candidateDocumentRepository.save(document);
//
//        log.info("Passport verified. documentId={}, candidateId={}",
//                documentId,
//                document.getCandidate().getId());
//    }
//
//    // =========================================================
//    // REJECT PASSPORT (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    public void rejectPassport(Long documentId, String rejectionReason) {
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Verified passport cannot be rejected.");
//        }
//
//        if (rejectionReason == null ||
//                rejectionReason.isBlank()) {
//
//            throw new BadRequestException(
//                    "Rejection reason is required.");
//        }
//
//        document.setStatus(DocumentStatus.REJECTED);
//
//        document.setRejectionReason(rejectionReason.trim());
//
//        candidateDocumentRepository.save(document);
//
//        log.info(
//                "Passport rejected. documentId={}, candidateId={}, reason={}",
//                documentId,
//                document.getCandidate().getId(),
//                rejectionReason);
//    }
//
//    // =========================================================
//    // PENDING PASSPORTS (HR/ADMIN)
//    // =========================================================
//
//    @Override
//    public List<CandidateDocumentDto> getPendingPassports() {
//
//        return candidateDocumentRepository
//                .findByStatus(DocumentStatus.PENDING)
//                .stream()
//                .filter(document ->
//                        document.getDocumentType()
//                                == DocumentType.PASSPORT)
//                .map(this::mapToDto)
//                .toList();
//    }
//
//
//
//    // =========================================================
//    // VALIDATE PASSPORT
//    // =========================================================
//
//    private void validatePassport(
//            MultipartFile passport) {
//
//        if (passport.isEmpty()) {
//
//            throw new BadRequestException("Passport file cannot be empty.");
//        }
//
//        if (passport.getSize() > MAX_FILE_SIZE) {
//
//            throw new BadRequestException("Passport file size cannot exceed 5 MB.");
//        }
//
//        String contentType = passport.getContentType();
//
//        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
//
//            throw new BadRequestException(
//                    "Invalid passport file type. " + "Only PDF, JPG, JPEG and PNG files are allowed.");
//        }
//    }
//
//    // =========================================================
//    // OWNERSHIP GUARD - ensures this service only ever
//    // touches documents of type PASSPORT, even though it
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
//        if (document.getDocumentType()
//                != DocumentType.PASSPORT) {
//
//            throw new BadRequestException("Document " + documentId + " is not a passport document.");
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
//        return candidateRepository.findById(candidateId)
//                .orElseThrow(() ->
//                        new BadRequestException("Candidate not found."));
//    }
//
//    // =========================================================
//    // GET AUTHENTICATED PRINCIPAL
//    // =========================================================
//
//    private UserPrincipal getAuthenticatedPrincipal() {
//
//        Authentication authentication = SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//
//        if (authentication == null || authentication.getPrincipal() == null) {
//
//            throw new BadRequestException("User is not authenticated.");
//        }
//
//        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
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
//    private String sanitizeFileName(String originalFileName) {
//
//        if (originalFileName == null || originalFileName.isBlank()) {
//
//            return "passport";
//        }
//
//        String fileName = originalFileName.replace("\\", "/");
//
//        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
//
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
//                .status(document.getStatus())
//                .rejectionReason(document.getRejectionReason())
//                .uploadedAt(document.getUploadedAt())
//                .updatedAt(document.getUpdatedAt())
//                .build();
//    }
//    @Override
//    public void updatePassport(Long documentId, MultipartFile passport) {
//
//        if (passport == null || passport.isEmpty()) {
//
//            throw new BadRequestException("Passport document is required.");}
//
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        /*
//         * Candidate can update only their own passport.
//         */
//        if (!document.getCandidate()
//                .getId()
//                .equals(candidate.getId())) {
//
//            throw new BadRequestException("You are not authorized to update this passport.");
//        }
//
//        /*
//         * A verified passport must never be replaced
//         * directly. The candidate would need to follow
//         * whatever re-verification workflow your business
//         * defines.
//         */
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Verified passport cannot be updated.");
//        }
//
//        validatePassport(passport);
//
//        try {
//            document.setFileName(sanitizeFileName(
//                    passport.getOriginalFilename()));
//
//            document.setContentType(
//                    passport.getContentType());
//
//            document.setDocumentData(
//                    passport.getBytes());
//            /*
//             * Any replacement must go through verification
//             * again.
//             */
//            document.setStatus(
//                    DocumentStatus.PENDING);
//            /*
//             * Clear old rejection information.
//             */
//            document.setRejectionReason(null);
//
//            candidateDocumentRepository.save(document);
//
//            log.info("Passport updated successfully. candidateId={}, documentId={}",
//                    candidate.getId(),
//                    documentId);
//
//        } catch (IOException exception) {
//
//            log.error("Failed to update passport. candidateId={}, documentId={}",
//                    candidate.getId(),
//                    documentId,
//                    exception);
//
//            throw new BadRequestException("Unable to update passport.");
//        }
//    }
//
//    @Override
//    public void deletePassport(Long documentId) {
//        Candidate candidate = getLoggedInCandidate();
//
//        CandidateDocument document = findOwnedDocumentOrThrow(documentId);
//
//        /*
//         * Candidate can delete only their own passport.
//         */
//        if (!document.getCandidate()
//                .getId()
//                .equals(candidate.getId())) {
//
//            throw new BadRequestException("You are not authorized to delete this passport.");
//        }
//        /*
//         * Never allow a verified passport to be deleted.
//         */
//        if (document.getStatus() == DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException("Verified passport cannot be deleted.");
//        }
//
//        candidateDocumentRepository.delete(document);
//
//        log.info("Passport deleted successfully. candidateId={}, documentId={}",
//                candidate.getId(), documentId);
//    }
//    }
