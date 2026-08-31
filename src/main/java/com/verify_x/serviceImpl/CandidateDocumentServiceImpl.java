package com.verify_x.serviceImpl;

import com.verify_x.dto.CandidateDashboardDto;
import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.CandidateDocumentRequest;
import com.verify_x.dto.DashboardStatisticsDto;
import com.verify_x.dto.HrVerificationRequestDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Employment;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
import com.verify_x.enums.VerificationStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateDocumentService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateDocumentServiceImpl
        implements CandidateDocumentService {

    private final CandidateRepository candidateRepository;

    private final CandidateDocumentRepository
            candidateDocumentRepository;


    // =========================================================
    // FILE CONFIGURATION
    // =========================================================

    private static final long MAX_FILE_SIZE =
            5L * 1024L * 1024L;


    private static final List<String> ALLOWED_TYPES =
            List.of(
                    "application/pdf",
                    "image/png",
                    "image/jpeg",
                    "image/jpg",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );


    // =========================================================
    // FRESHER DOCUMENTS
    // =========================================================

    private static final Set<DocumentType> FRESHER_DOCUMENTS =
            Set.of(
                    DocumentType.RESUME,
                    DocumentType.CURRENT_OFFERLETTER,
                    DocumentType.AADHAAR_CARD,
                    DocumentType.PAN_CARD
            );


    // =========================================================
    // EXPERIENCED DOCUMENTS
    // =========================================================

    private static final Set<DocumentType> EXPERIENCED_DOCUMENTS =
            Set.of(
                    DocumentType.RESUME,
                    DocumentType.CURRENT_OFFERLETTER,
                    DocumentType.SALARY_SLIP,
                    DocumentType.PAN_CARD,
                    DocumentType.AADHAAR_CARD,
                    DocumentType.UAN_PROOF,
                    DocumentType.EXPERIENCE_LETTER,
                    DocumentType.RELIEVING_LETTER
            );


    // =========================================================
    // TRAVEL DOCUMENTS
    //
    // Passport and Visa are OPTIONAL and available for
    // BOTH FRESHER and EXPERIENCED candidates.
    // =========================================================

    private static final Set<DocumentType> TRAVEL_DOCUMENTS =
            Set.of(
                    DocumentType.PASSPORT,
                    DocumentType.VISA
            );


    // =========================================================
    // DTO MAPPING
    // =========================================================

    private CandidateDocumentDto mapToDto(
            CandidateDocument document) {

        return CandidateDocumentDto.builder()

                .id(document.getId())

                .documentType(
                        document.getDocumentType())

                .fileName(
                        document.getFileName())

                .contentType(
                        document.getContentType())

                .status(
                        document.getStatus())

                .rejectionReason(
                        document.getRejectionReason())

                .uploadedAt(
                        document.getUploadedAt())

                .updatedAt(
                        document.getUpdatedAt())

                .build();
    }


    // =========================================================
    // GET LOGGED-IN CANDIDATE
    // =========================================================

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null ||
                authentication.getPrincipal() == null) {

            throw new BadRequestException(
                    "User is not authenticated.");
        }


        if (!(authentication.getPrincipal()
                instanceof UserPrincipal)) {

            throw new BadRequestException(
                    "Invalid authentication details.");
        }


        UserPrincipal principal =
                (UserPrincipal)
                        authentication.getPrincipal();


        if (principal.getUserId() == null) {

            throw new BadRequestException(
                    "Candidate information is missing from authentication.");
        }


        return candidateRepository
                .findById(principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Candidate not found."));
    }


    // =========================================================
    // FILE VALIDATION
    // =========================================================

    private void validateFile(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new BadRequestException(
                    "Please upload a document.");
        }


        if (file.getSize() > MAX_FILE_SIZE) {

            throw new BadRequestException(
                    "Maximum allowed size is 5 MB.");
        }


        String contentType =
                file.getContentType();


        if (contentType == null ||
                !ALLOWED_TYPES.contains(
                        contentType.toLowerCase())) {

            throw new BadRequestException(
                    "Only PDF, DOC, DOCX, PNG, JPG and JPEG files are allowed.");
        }
    }


    // =========================================================
    // VALIDATE DOCUMENT TYPE
    // =========================================================

    private void validateDocumentType(
            Candidate candidate,
            DocumentType documentType) {

        /*
         * Passport and Visa are optional travel documents.
         *
         * They are allowed for every candidate type.
         */
        if (TRAVEL_DOCUMENTS.contains(
                documentType)) {

            return;
        }


        Set<DocumentType> allowedDocuments;


        if (candidate.getCandidateType()
                == CandidateType.FRESHER) {

            allowedDocuments =
                    FRESHER_DOCUMENTS;

        } else {

            allowedDocuments =
                    EXPERIENCED_DOCUMENTS;
        }


        if (!allowedDocuments.contains(
                documentType)) {

            throw new BadRequestException(
                    documentType +
                            " is not allowed for " +
                            candidate.getCandidateType() +
                            " candidate.");
        }
    }


    // =========================================================
    // UPLOAD SINGLE DOCUMENT
    //
    // Used for:
    // Resume
    // PAN
    // Aadhaar
    // Passport
    // Visa
    // etc.
    //
    // If file is null/empty, nothing happens.
    // Therefore Passport/Visa remain OPTIONAL.
    // =========================================================

    private void uploadIfPresent(
            Candidate candidate,
            MultipartFile file,
            DocumentType documentType) {

        /*
         * Optional document.
         */
        if (file == null ||
                file.isEmpty()) {

            return;
        }


        validateFile(file);


        validateDocumentType(
                candidate,
                documentType);


        try {

            CandidateDocument document =
                    candidateDocumentRepository
                            .findByCandidateAndDocumentType(
                                    candidate,
                                    documentType)
                            .orElseGet(() ->
                                    CandidateDocument
                                            .builder()
                                            .candidate(candidate)
                                            .documentType(
                                                    documentType)
                                            .build());


            /*
             * Verified documents cannot be overwritten.
             */
            if (document.getId() != null &&
                    document.getStatus()
                            == DocumentStatus.VERIFIED) {

                throw new BadRequestException(
                        documentType +
                                " is already verified and cannot be replaced.");
            }


            setDocumentData(
                    document,
                    file);


            /*
             * New upload/replacement always requires
             * HR verification.
             */
            document.setStatus(
                    DocumentStatus.PENDING);


            /*
             * Clear previous rejection.
             */
            document.setRejectionReason(
                    null);


            candidateDocumentRepository.save(
                    document);


            log.info(
                    "{} uploaded successfully. candidateId={}, documentId={}",
                    documentType,
                    candidate.getId(),
                    document.getId());

        } catch (IOException exception) {

            log.error(
                    "Error uploading {} for candidate {}",
                    documentType,
                    candidate.getId(),
                    exception);

            throw new BadRequestException(
                    "Unable to upload " +
                            documentType +
                            ".");
        }
    }


    // =========================================================
    // UPLOAD DOCUMENTS
    // =========================================================

    @Override
    public void uploadDocuments(
            CandidateDocumentRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Document request cannot be null.");
        }


        Candidate candidate =
                getLoggedInCandidate();


        // Existing documents
        uploadIfPresent(
                candidate,
                request.getResume(),
                DocumentType.RESUME);


        uploadIfPresent(
                candidate,
                request.getOfferLetter(),
                DocumentType.CURRENT_OFFERLETTER);


        uploadIfPresent(
                candidate,
                request.getSalarySlip(),
                DocumentType.SALARY_SLIP);


        uploadIfPresent(
                candidate,
                request.getPanCard(),
                DocumentType.PAN_CARD);


        uploadIfPresent(
                candidate,
                request.getAadhaarCard(),
                DocumentType.AADHAAR_CARD);


        uploadIfPresent(
                candidate,
                request.getUanProof(),
                DocumentType.UAN_PROOF);


        uploadIfPresent(
                candidate,
                request.getExperienceLetter(),
                DocumentType.EXPERIENCE_LETTER);


        uploadIfPresent(
                candidate,
                request.getRelievingLetter(),
                DocumentType.RELIEVING_LETTER);


        // =====================================================
        // OPTIONAL PASSPORT
        // =====================================================

        uploadIfPresent(
                candidate,
                request.getPassport(),
                DocumentType.PASSPORT);


        // =====================================================
        // OPTIONAL VISA
        // =====================================================

        uploadIfPresent(
                candidate,
                request.getVisa(),
                DocumentType.VISA);


        updateApplicationStatus(
                candidate);


        log.info(
                "Documents uploaded successfully by {}",
                candidate.getEmail());
    }


    // =========================================================
    // RE-UPLOAD DOCUMENTS
    //
    // Existing behavior:
    // only REJECTED documents can be re-uploaded.
    //
    // Passport/Visa follow the SAME workflow.
    // =========================================================

    @Override
    public void reUploadDocuments(
            CandidateDocumentRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Document request cannot be null.");
        }


        Candidate candidate =
                getLoggedInCandidate();


        reUploadIfPresent(
                candidate,
                request.getResume(),
                DocumentType.RESUME);


        reUploadIfPresent(
                candidate,
                request.getOfferLetter(),
                DocumentType.CURRENT_OFFERLETTER);


        reUploadIfPresent(
                candidate,
                request.getSalarySlip(),
                DocumentType.SALARY_SLIP);


        reUploadIfPresent(
                candidate,
                request.getPanCard(),
                DocumentType.PAN_CARD);


        reUploadIfPresent(
                candidate,
                request.getAadhaarCard(),
                DocumentType.AADHAAR_CARD);


        reUploadIfPresent(
                candidate,
                request.getUanProof(),
                DocumentType.UAN_PROOF);


        reUploadIfPresent(
                candidate,
                request.getExperienceLetter(),
                DocumentType.EXPERIENCE_LETTER);


        reUploadIfPresent(
                candidate,
                request.getRelievingLetter(),
                DocumentType.RELIEVING_LETTER);


        // =====================================================
        // OPTIONAL PASSPORT RE-UPLOAD
        // =====================================================

        reUploadIfPresent(
                candidate,
                request.getPassport(),
                DocumentType.PASSPORT);


        // =====================================================
        // OPTIONAL VISA RE-UPLOAD
        // =====================================================

        reUploadIfPresent(
                candidate,
                request.getVisa(),
                DocumentType.VISA);


        updateApplicationStatus(
                candidate);


        log.info(
                "Documents re-uploaded successfully by {}",
                candidate.getEmail());
    }


    // =========================================================
    // RE-UPLOAD SINGLE DOCUMENT
    // =========================================================

    private void reUploadIfPresent(
            Candidate candidate,
            MultipartFile file,
            DocumentType documentType) {

        if (file == null ||
                file.isEmpty()) {

            return;
        }


        validateFile(file);


        validateDocumentType(
                candidate,
                documentType);


        CandidateDocument document =
                candidateDocumentRepository
                        .findByCandidateAndDocumentType(
                                candidate,
                                documentType)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document",
                                        null));


        /*
         * Verified documents can never be re-uploaded.
         */
        if (document.getStatus()
                == DocumentStatus.VERIFIED) {

            throw new BadRequestException(
                    documentType +
                            " is already verified and cannot be replaced.");
        }


        /*
         * Re-upload is intended for rejected documents.
         */
        if (document.getStatus()
                != DocumentStatus.REJECTED) {

            throw new BadRequestException(
                    documentType +
                            " is not rejected. " +
                            "Only rejected documents can be re-uploaded.");
        }


        try {

            setDocumentData(
                    document,
                    file);


            document.setStatus(
                    DocumentStatus.PENDING);


            document.setRejectionReason(
                    null);


            candidateDocumentRepository.save(
                    document);


            log.info(
                    "{} re-uploaded successfully. candidateId={}, documentId={}",
                    documentType,
                    candidate.getId(),
                    document.getId());

        } catch (IOException exception) {

            log.error(
                    "Error re-uploading {} for candidate {}",
                    documentType,
                    candidate.getId(),
                    exception);

            throw new BadRequestException(
                    "Unable to re-upload " +
                            documentType +
                            ".");
        }
    }


    // =========================================================
    // GET MY DOCUMENTS
    // =========================================================

    @Override
    public List<CandidateDocumentDto>
    getMyDocuments() {

        Candidate candidate =
                getLoggedInCandidate();


        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    // =========================================================
    // GET CANDIDATE DOCUMENTS
    // HR / ADMIN
    // =========================================================

    @Override
    public List<CandidateDocumentDto>
    getDocumentsByCandidateId(
            Long candidateId) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required.");
        }


        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));


        return candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    // =========================================================
    // GET DOCUMENT
    // =========================================================

    @Override
    public CandidateDocument getDocument(
            Long documentId) {

        CandidateDocument document =
                findDocument(documentId);


        /*
         * Candidate may view only their own document.
         * HR/Admin can view any document.
         */
        validateDocumentAccess(
                document);


        return document;
    }


    // =========================================================
    // DELETE DOCUMENT
    // =========================================================

    @Override
    public void deleteDocument(
            Long documentId) {

        Candidate candidate =
                getLoggedInCandidate();


        CandidateDocument document =
                findDocument(documentId);


        /*
         * Ownership check.
         */
        if (!document.getCandidate()
                .getId()
                .equals(candidate.getId())) {

            throw new BadRequestException(
                    "You are not allowed to delete this document.");
        }


        /*
         * Verified documents cannot be deleted.
         */
        if (document.getStatus()
                == DocumentStatus.VERIFIED) {

            throw new BadRequestException(
                    "Verified documents cannot be deleted.");
        }


        candidateDocumentRepository.delete(
                document);


        /*
         * Recalculate candidate application status.
         */
        updateApplicationStatus(
                candidate);


        log.info(
                "{} deleted by candidate {}",
                document.getDocumentType(),
                candidate.getEmail());
    }


    // =========================================================
    // DOWNLOAD DOCUMENT
    // =========================================================

    @Override
    public Resource downloadDocument(
            Long documentId) {

        CandidateDocument document =
                findDocument(documentId);


        validateDocumentAccess(
                document);


        if (document.getDocumentData() == null ||
                document.getDocumentData().length == 0) {

            throw new BadRequestException(
                    "Document data is empty.");
        }


        return new ByteArrayResource(
                document.getDocumentData());
    }


    // =========================================================
    // HR VERIFY DOCUMENT
    // =========================================================

    @Override
    public void verifyDocument(
            Long documentId) {

        CandidateDocument document =
                findDocument(documentId);


        if (document.getStatus()
                == DocumentStatus.VERIFIED) {

            throw new BadRequestException(
                    "Document is already verified.");
        }


        /*
         * Only pending documents should be verified.
         */
        if (document.getStatus()
                != DocumentStatus.PENDING) {

            throw new BadRequestException(
                    "Only pending documents can be verified.");
        }


        document.setStatus(
                DocumentStatus.VERIFIED);


        document.setRejectionReason(
                null);


        candidateDocumentRepository.save(
                document);


        updateApplicationStatus(
                document.getCandidate());


        log.info(
                "{} verified successfully for candidate {}",
                document.getDocumentType(),
                document.getCandidate().getEmail());
    }


    // =========================================================
    // HR REJECT DOCUMENT
    // =========================================================

    @Override
    public void rejectDocument(
            Long documentId,
            String rejectionReason) {

        CandidateDocument document =
                findDocument(documentId);


        if (document.getStatus()
                == DocumentStatus.VERIFIED) {

            throw new BadRequestException(
                    "Verified document cannot be rejected.");
        }


        if (document.getStatus()
                != DocumentStatus.PENDING) {

            throw new BadRequestException(
                    "Only pending documents can be rejected.");
        }


        if (rejectionReason == null ||
                rejectionReason.isBlank()) {

            throw new BadRequestException(
                    "Rejection reason is required.");
        }


        String reason =
                rejectionReason.trim();


        if (reason.length() > 500) {

            throw new BadRequestException(
                    "Rejection reason cannot exceed 500 characters.");
        }


        document.setStatus(
                DocumentStatus.REJECTED);


        document.setRejectionReason(
                reason);


        candidateDocumentRepository.save(
                document);


        updateApplicationStatus(
                document.getCandidate());


        log.info(
                "{} rejected for candidate {}",
                document.getDocumentType(),
                document.getCandidate().getEmail());
    }


    // =========================================================
    // HR PENDING DOCUMENTS
    // =========================================================

    @Override
    public List<CandidateDocumentDto>
    getPendingDocuments() {

        return candidateDocumentRepository
                .findByStatus(
                        DocumentStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    // =========================================================
    // HR DASHBOARD STATISTICS
    // =========================================================

    @Override
    public DashboardStatisticsDto
    getDashboardStatistics() {

        return DashboardStatisticsDto.builder()

                .totalDocuments(
                        candidateDocumentRepository.count())

                .verified(
                        candidateDocumentRepository
                                .countByStatus(
                                        DocumentStatus.VERIFIED))

                .pending(
                        candidateDocumentRepository
                                .countByStatus(
                                        DocumentStatus.PENDING))

                .rejected(
                        candidateDocumentRepository
                                .countByStatus(
                                        DocumentStatus.REJECTED))

                .build();
    }


    // =========================================================
    // HR CANDIDATE DASHBOARD
    // =========================================================

    @Override
    public List<CandidateDashboardDto>
    getCandidateDashboard() {

        List<Candidate> candidates =
                candidateRepository.findAll();


        List<CandidateDashboardDto> dashboard =
                new ArrayList<>();


        for (Candidate candidate : candidates) {

            List<CandidateDocument> documents =
                    candidateDocumentRepository
                            .findByCandidate(candidate);


            long verified =
                    documents.stream()
                            .filter(document ->
                                    document.getStatus()
                                            == DocumentStatus.VERIFIED)
                            .count();


            long pending =
                    documents.stream()
                            .filter(document ->
                                    document.getStatus()
                                            == DocumentStatus.PENDING)
                            .count();


            long rejected =
                    documents.stream()
                            .filter(document ->
                                    document.getStatus()
                                            == DocumentStatus.REJECTED)
                            .count();


            dashboard.add(

                    CandidateDashboardDto.builder()

                            .candidateId(
                                    candidate.getId())

                            .candidateName(
                                    candidate.getUsername())

                            .email(
                                    candidate.getEmail())

                            .phoneNumber(
                                    candidate.getPhoneNumber())

                            .candidateType(
                                    candidate.getCandidateType())

                            .totalDocuments(
                                    documents.size())

                            .verifiedDocuments(
                                    verified)

                            .pendingDocuments(
                                    pending)

                            .rejectedDocuments(
                                    rejected)

                            .build());
        }


        return dashboard;
    }


    // =========================================================
    // VERIFY UAN
    // =========================================================

    @Override
    public void verifyUan(
            Long candidateId) {

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));


        Employment employment =
                candidate.getEmployment();


        if (employment == null) {

            throw new BadRequestException(
                    "Employment details not found.");
        }


        employment.setUanVerificationStatus(
                VerificationStatus.VERIFIED);


        candidateRepository.save(
                candidate);


        log.info(
                "UAN verified successfully for candidate {}",
                candidateId);
    }


    // =========================================================
    // HR VERIFICATION REQUESTS
    // =========================================================

    @Override
    public List<HrVerificationRequestDto>
    getAllVerificationRequests() {

        return candidateRepository
                .findAll()
                .stream()

                .filter(candidate ->
                        candidateDocumentRepository
                                .countByCandidate(candidate) > 0)

                .map(candidate -> {

                    List<CandidateDocument> documents =
                            candidateDocumentRepository
                                    .findByCandidate(candidate);


                    int count =
                            documents.size();


                    DocumentStatus overallStatus;


                    boolean rejected =
                            documents.stream()
                                    .anyMatch(document ->
                                            document.getStatus()
                                                    == DocumentStatus.REJECTED);


                    boolean pending =
                            documents.stream()
                                    .anyMatch(document ->
                                            document.getStatus()
                                                    == DocumentStatus.PENDING);


                    if (rejected) {

                        overallStatus =
                                DocumentStatus.REJECTED;

                    } else if (pending) {

                        overallStatus =
                                DocumentStatus.PENDING;

                    } else {

                        overallStatus =
                                DocumentStatus.VERIFIED;
                    }


                    VerificationStatus uanStatus =
                            VerificationStatus.PENDING;


                    if (candidate.getEmployment() != null &&
                            candidate.getEmployment()
                                    .getUanVerificationStatus() != null) {

                        uanStatus =
                                candidate.getEmployment()
                                        .getUanVerificationStatus();
                    }


                    return HrVerificationRequestDto.builder()

                            .candidateId(
                                    candidate.getId())

                            .candidateName(
                                    candidate.getUsername())

                            .email(
                                    candidate.getEmail())

                            .candidateType(
                                    candidate.getCandidateType())

                            .documentCount(
                                    count)

                            .uanVerificationStatus(
                                    uanStatus)

                            .status(
                                    overallStatus)

                            .build();

                })
                .toList();
    }


    // =========================================================
    // FIND DOCUMENT
    // =========================================================

    private CandidateDocument findDocument(
            Long documentId) {

        if (documentId == null) {

            throw new BadRequestException(
                    "Document ID is required.");
        }


        return candidateDocumentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document",
                                documentId));
    }


    // =========================================================
    // DOCUMENT ACCESS
    // =========================================================

    private void validateDocumentAccess(
            CandidateDocument document) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null ||
                authentication.getPrincipal() == null) {

            throw new BadRequestException(
                    "User is not authenticated.");
        }


        if (!(authentication.getPrincipal()
                instanceof UserPrincipal)) {

            throw new BadRequestException(
                    "Invalid authentication details.");
        }


        UserPrincipal principal =
                (UserPrincipal)
                        authentication.getPrincipal();


        String role =
                principal.getRole().name();


        /*
         * HR and ADMIN can view any candidate document.
         */
        if ("HR".equals(role) ||
                "ADMIN".equals(role)) {

            return;
        }


        /*
         * Candidate can only view/download
         * their own document.
         */
        if ("CANDIDATE".equals(role)) {

            if (!document.getCandidate()
                    .getId()
                    .equals(principal.getUserId())) {

                throw new BadRequestException(
                        "You are not authorized to access this document.");
            }

            return;
        }


        throw new BadRequestException(
                "You are not authorized to access this document.");
    }


    // =========================================================
    // SET DOCUMENT DATA
    // =========================================================

    private void setDocumentData(
            CandidateDocument document,
            MultipartFile file)
            throws IOException {

        String fileName =
                sanitizeFileName(
                        file.getOriginalFilename());


        document.setFileName(
                fileName);


        document.setContentType(
                file.getContentType());


        document.setDocumentData(
                file.getBytes());
    }


    // =========================================================
    // FILE NAME SANITIZATION
    // =========================================================

    private String sanitizeFileName(
            String originalFileName) {

        if (originalFileName == null ||
                originalFileName.isBlank()) {

            return "document";
        }


        String fileName =
                originalFileName
                        .replace("\\", "/");


        fileName =
                fileName.substring(
                        fileName.lastIndexOf("/") + 1);


        fileName =
                fileName.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_");


        if (fileName.length() > 255) {

            fileName =
                    fileName.substring(
                            fileName.length() - 255);
        }


        return fileName;
    }


    // =========================================================
    // APPLICATION STATUS
    // =========================================================
    private void updateApplicationStatus(
            Candidate candidate) {

        List<CandidateDocument> documents =
                candidateDocumentRepository
                        .findByCandidate(candidate)
                        .stream()
                        .filter(document ->
                                document.getDocumentType() != DocumentType.PASSPORT
                                        &&
                                        document.getDocumentType() != DocumentType.VISA)
                        .toList();


        if (documents.isEmpty()) {

            candidate.setApplicationStatus(
                    ApplicationStatus.PENDING_VERIFICATION);

            candidateRepository.save(candidate);

            return;
        }


        boolean rejected =
                documents.stream()
                        .anyMatch(document ->
                                document.getStatus()
                                        == DocumentStatus.REJECTED);


        boolean pending =
                documents.stream()
                        .anyMatch(document ->
                                document.getStatus()
                                        == DocumentStatus.PENDING);


        if (rejected) {

            candidate.setApplicationStatus(
                    ApplicationStatus.RE_UPLOAD_REQUIRED);

        } else if (pending) {

            candidate.setApplicationStatus(
                    ApplicationStatus.PENDING_VERIFICATION);

        } else {

            candidate.setApplicationStatus(
                    ApplicationStatus.DOCUMENTS_VERIFIED);
        }


        candidateRepository.save(candidate);
    }
}







//package com.verify_x.serviceImpl;
//
//import com.verify_x.dto.CandidateDashboardDto;
//import com.verify_x.dto.CandidateDocumentDto;
//import com.verify_x.dto.CandidateDocumentRequest;
//import com.verify_x.dto.DashboardStatisticsDto;
//import com.verify_x.dto.HrVerificationRequestDto;
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.CandidateDocument;
//import com.verify_x.entity.Employment;
//import com.verify_x.enums.ApplicationStatus;
//import com.verify_x.enums.CandidateType;
//import com.verify_x.enums.DocumentStatus;
//import com.verify_x.enums.DocumentType;
//import com.verify_x.enums.VerificationStatus;
//import com.verify_x.exception.BadRequestException;
//import com.verify_x.exception.ResourceNotFoundException;
//import com.verify_x.jwt.UserPrincipal;
//import com.verify_x.repository.CandidateDocumentRepository;
//import com.verify_x.repository.CandidateRepository;
//import com.verify_x.services.CandidateDocumentService;
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
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//public class CandidateDocumentServiceImpl implements CandidateDocumentService {
//
//    private final CandidateRepository candidateRepository;
//    private final CandidateDocumentRepository candidateDocumentRepository;
//
//    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
//
//    private static final List<String> ALLOWED_TYPES = List.of(
//            "application/pdf",
//            "image/png",
//            "image/jpeg",
//            "image/jpg",
//            "application/msword",
//            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
//    );
//
//    private static final Set<DocumentType> FRESHER_DOCUMENTS = Set.of(
//            DocumentType.RESUME,
//            DocumentType.CURRENT_OFFERLETTER,
//            DocumentType.AADHAAR_CARD,
//            DocumentType.PAN_CARD
//    );
//
//    private static final Set<DocumentType> EXPERIENCED_DOCUMENTS = Set.of(
//            DocumentType.RESUME,
//            DocumentType.CURRENT_OFFERLETTER,
//            DocumentType.SALARY_SLIP,
//            DocumentType.PAN_CARD,
//            DocumentType.AADHAAR_CARD,
//            DocumentType.UAN_PROOF,
//            DocumentType.EXPERIENCE_LETTER,
//            DocumentType.RELIEVING_LETTER
//    );
//
//    //map to dto
//    private CandidateDocumentDto mapToDto(CandidateDocument document) {
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
//
//    //get logge din candidate
//    private Candidate getLoggedInCandidate() {
//
//        Authentication authentication =
//                SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//
//        if (authentication == null ||
//                authentication.getPrincipal() == null) {
//
//            throw new BadRequestException(
//                    "User is not authenticated."
//            );
//        }
//
//        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
//
//            throw new BadRequestException(
//                    "Invalid authentication details."
//            );
//        }
//
//        UserPrincipal principal =
//                (UserPrincipal) authentication.getPrincipal();
//
//        return candidateRepository
//                .findById(principal.getUserId())
//                .orElseThrow(() ->
//                        new UsernameNotFoundException(
//                                "Candidate not found."
//                        ));
//    }
//
//    private void validateFile(MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//
//            throw new BadRequestException(
//                    "Please upload a document."
//            );
//        }
//
//        if (file.getSize() > MAX_FILE_SIZE) {
//
//            throw new BadRequestException(
//                    "Maximum allowed size is 5 MB."
//            );
//        }
//
//        String contentType = file.getContentType();
//
//        if (contentType == null ||
//                !ALLOWED_TYPES.contains(
//                        contentType.toLowerCase()
//                )) {
//
//            throw new BadRequestException(
//                    "Only PDF, DOC, DOCX, PNG, JPG and JPEG files are allowed."
//            );
//        }
//    }
//    //validate document type on candidate
//    private void validateDocumentType(
//            Candidate candidate,
//            DocumentType documentType
//    ) {
//
//        Set<DocumentType> allowedDocuments;
//
//        if (candidate.getCandidateType() == CandidateType.FRESHER) {
//
//            allowedDocuments = FRESHER_DOCUMENTS;
//
//        } else {
//
//            allowedDocuments = EXPERIENCED_DOCUMENTS;
//        }
//
//        if (!allowedDocuments.contains(documentType)) {
//
//            throw new BadRequestException(
//                    documentType +
//                            " is not allowed for " +
//                            candidate.getCandidateType() +
//                            " candidate."
//            );
//        }
//    }
//
//   //uploda single docs
//    private void uploadIfPresent(
//            Candidate candidate,
//            MultipartFile file,
//            DocumentType documentType
//    ) {
//
//        if (file == null || file.isEmpty()) {
//            return;
//        }
//
//        validateFile(file);
//
//        validateDocumentType(
//                candidate,
//                documentType
//        );
//
//        try {
//
//            CandidateDocument document =
//                    candidateDocumentRepository
//                            .findByCandidateAndDocumentType(
//                                    candidate,
//                                    documentType
//                            )
//                            .orElseGet(() ->
//                                    CandidateDocument.builder()
//                                            .candidate(candidate)
//                                            .documentType(documentType)
//                                            .build()
//                            );
//
////verified document cannot to replaced
//            if (document.getId() != null &&
//                    document.getStatus() == DocumentStatus.VERIFIED) {
//
//                throw new BadRequestException(
//                        documentType +
//                                " is already verified and cannot be replaced."
//                );
//            }
//
//            document.setFileName(
//                    file.getOriginalFilename()
//            );
//
//            document.setContentType(
//                    file.getContentType()
//            );
//
//            document.setDocumentData(
//                    file.getBytes()
//            );
//
//            document.setStatus(
//                    DocumentStatus.PENDING
//            );
//
//            document.setRejectionReason(null);
//
//            candidateDocumentRepository.save(document);
//
//        } catch (IOException e) {
//
//            log.error(
//                    "Error uploading {} for candidate {}",
//                    documentType,
//                    candidate.getId(),
//                    e
//            );
//
//            throw new BadRequestException(
//                    "Unable to upload " +
//                            documentType +
//                            "."
//            );
//        }
//    }
//
//
//    @Override
//    public void uploadDocuments(
//            CandidateDocumentRequest request
//    ) {
//
//        if (request == null) {
//
//            throw new BadRequestException(
//                    "Document request cannot be null."
//            );
//        }
//
//        Candidate candidate =
//                getLoggedInCandidate();
//
//        uploadIfPresent(
//                candidate,
//                request.getResume(),
//                DocumentType.RESUME
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getOfferLetter(),
//                DocumentType.CURRENT_OFFERLETTER
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getSalarySlip(),
//                DocumentType.SALARY_SLIP
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getPanCard(),
//                DocumentType.PAN_CARD
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getAadhaarCard(),
//                DocumentType.AADHAAR_CARD
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getUanProof(),
//                DocumentType.UAN_PROOF
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getExperienceLetter(),
//                DocumentType.EXPERIENCE_LETTER
//        );
//
//        uploadIfPresent(
//                candidate,
//                request.getRelievingLetter(),
//                DocumentType.RELIEVING_LETTER
//        );
//
//        updateApplicationStatus(candidate);
//
//        log.info(
//                "Documents uploaded successfully by {}",
//                candidate.getEmail()
//        );
//    }
//
//    @Override
//    public void reUploadDocuments(
//            CandidateDocumentRequest request
//    ) {
//
//        if (request == null) {
//
//            throw new BadRequestException(
//                    "Document request cannot be null."
//            );
//        }
//
//        Candidate candidate =
//                getLoggedInCandidate();
//
//        reUploadIfPresent(
//                candidate,
//                request.getResume(),
//                DocumentType.RESUME
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getOfferLetter(),
//                DocumentType.CURRENT_OFFERLETTER
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getSalarySlip(),
//                DocumentType.SALARY_SLIP
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getPanCard(),
//                DocumentType.PAN_CARD
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getAadhaarCard(),
//                DocumentType.AADHAAR_CARD
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getUanProof(),
//                DocumentType.UAN_PROOF
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getExperienceLetter(),
//                DocumentType.EXPERIENCE_LETTER
//        );
//
//        reUploadIfPresent(
//                candidate,
//                request.getRelievingLetter(),
//                DocumentType.RELIEVING_LETTER
//        );
//
//        updateApplicationStatus(candidate);
//
//        log.info(
//                "Documents re-uploaded successfully by {}",
//                candidate.getEmail()
//        );
//    }
//
//    //reupload single document if present
//    private void reUploadIfPresent(
//            Candidate candidate,
//            MultipartFile file,
//            DocumentType documentType
//    ) {
//
//        if (file == null || file.isEmpty()) {
//            return;
//        }
//
//        validateFile(file);
//
//        validateDocumentType(
//                candidate,
//                documentType
//        );
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findByCandidateAndDocumentType(
//                                candidate,
//                                documentType
//                        )
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Document",
//                                        null
//                                )
//                        );
//
//        //only rejected docs allowed to re-upload
//        if (document.getStatus() !=
//                DocumentStatus.REJECTED) {
//
//            throw new BadRequestException(
//                    documentType +
//                            " is not rejected. " +
//                            "Only rejected documents can be re-uploaded."
//            );
//        }
//
//        try {
//
//            document.setFileName(
//                    file.getOriginalFilename()
//            );
//
//            document.setContentType(
//                    file.getContentType()
//            );
//
//            document.setDocumentData(
//                    file.getBytes()
//            );
//
//            // After re-upload it goes back to PENDING
//            document.setStatus(
//                    DocumentStatus.PENDING
//            );
//
//            // Clear old rejection reason
//            document.setRejectionReason(null);
//
//            candidateDocumentRepository.save(document);
//
//        } catch (IOException e) {
//
//            log.error(
//                    "Error re-uploading {} for candidate {}",
//                    documentType,
//                    candidate.getId(),
//                    e
//            );
//
//            throw new BadRequestException(
//                    "Unable to re-upload " +
//                            documentType +
//                            "."
//            );
//        }
//    }
//
//    @Override
//    public List<CandidateDocumentDto> getMyDocuments() {
//
//        Candidate candidate =
//                getLoggedInCandidate();
//
//        return candidateDocumentRepository
//                .findByCandidate(candidate)
//                .stream()
//                .map(this::mapToDto)
//                .toList();
//    }
//
//    @Override
//    public List<CandidateDocumentDto> getDocumentsByCandidateId(
//            Long candidateId
//    ) {
//
//        Candidate candidate =
//                candidateRepository
//                        .findById(candidateId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Candidate",
//                                        candidateId
//                                )
//                        );
//
//        return candidateDocumentRepository
//                .findByCandidate(candidate)
//                .stream()
//                .map(this::mapToDto)
//                .toList();
//    }
//
//
//    @Override
//    public CandidateDocument getDocument(
//            Long documentId
//    ) {
//
//        return candidateDocumentRepository
//                .findById(documentId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Document",
//                                documentId
//                        )
//                );
//    }
//
//
//    @Override
//    public void deleteDocument(
//            Long documentId
//    ) {
//
//        Candidate candidate =
//                getLoggedInCandidate();
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Document",
//                                        documentId
//                                )
//                        );
//
//        log.info(
//                "Logged-in Candidate ID : {}",
//                candidate.getId()
//        );
//
//        log.info(
//                "Document Owner ID      : {}",
//                document.getCandidate().getId()
//        );
//
//        log.info(
//                "Document ID            : {}",
//                document.getId()
//        );
//
////ckeck docs owner
//        if (!document.getCandidate()
//                .getId()
//                .equals(candidate.getId())) {
//
//            throw new BadRequestException(
//                    "You are not allowed to delete this document."
//            );
//        }
//
////verified docs cannot be deleted
//        if (document.getStatus() ==
//                DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException(
//                    "Verified documents cannot be deleted."
//            );
//        }
//
//        candidateDocumentRepository.delete(document);
//
//        updateApplicationStatus(candidate);
//
//        log.info(
//                "{} deleted by {}",
//                document.getDocumentType(),
//                candidate.getEmail()
//        );
//    }
//
//    //download document by id
//    @Override
//    public Resource downloadDocument(
//            Long documentId
//    ) {
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Document",
//                                        documentId
//                                )
//                        );
//
//        Authentication authentication =
//                SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//
//        if (authentication == null ||
//                authentication.getPrincipal() == null) {
//
//            throw new BadRequestException(
//                    "User is not authenticated."
//            );
//        }
//
//        if (!(authentication.getPrincipal()
//                instanceof UserPrincipal)) {
//
//            throw new BadRequestException(
//                    "Invalid authentication details."
//            );
//        }
//
//        UserPrincipal principal =
//                (UserPrincipal) authentication.getPrincipal();
//
//        //candidate can only download their own documents
//        if ("CANDIDATE".equals(
//                principal.getRole().name())) {
//
//            if (!document.getCandidate()
//                    .getId()
//                    .equals(principal.getUserId())) {
//
//                throw new BadRequestException(
//                        "You are not authorized to download this document."
//                );
//            }
//        }
//
//        return new ByteArrayResource(
//                document.getDocumentData()
//        );
//    }
//
////veri
//    @Override
//    public void verifyDocument(
//            Long documentId
//    ) {
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Document",
//                                        documentId
//                                )
//                        );
//
//        //already verified docs cannot be verified again
//        if (document.getStatus() ==
//                DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException(
//                    "Document is already verified."
//            );
//        }
//        //verified docs cannot be rejected
//        document.setStatus(
//                DocumentStatus.VERIFIED
//        );
//
//        document.setRejectionReason(null);
//
//        candidateDocumentRepository.save(document);
//
//        // Update candidate application status
//        updateApplicationStatus(
//                document.getCandidate()
//        );
//
//        log.info(
//                "{} verified successfully.",
//                document.getDocumentType()
//        );
//    }
//
//   //reject document by id
//    @Override
//    public void rejectDocument(
//            Long documentId,
//            String rejectionReason
//    ) {
//
//        CandidateDocument document =
//                candidateDocumentRepository
//                        .findById(documentId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Document",
//                                        documentId
//                                )
//                        );
//
//        //verified docs cannot be rejected
//        if (document.getStatus() ==
//                DocumentStatus.VERIFIED) {
//
//            throw new BadRequestException(
//                    "Verified document cannot be rejected."
//            );
//        }
//        //rejection reason is required
//        if (rejectionReason == null ||
//                rejectionReason.isBlank()) {
//
//            throw new BadRequestException(
//                    "Rejection reason is required."
//            );
//        }
//
//        //set document status to REJECTED and save rejection reason
//        document.setStatus(
//                DocumentStatus.REJECTED
//        );
//
//        document.setRejectionReason(
//                rejectionReason.trim()
//        );
//
//        candidateDocumentRepository.save(document);
//
//        // Update candidate application status
//        updateApplicationStatus(
//                document.getCandidate()
//        );
//
//        log.info(
//                "{} rejected for candidate {}.",
//                document.getDocumentType(),
//                document.getCandidate().getEmail()
//        );
//    }
//
//    //get all pending documents for HR verification
//    @Override
//    public List<CandidateDocumentDto> getPendingDocuments() {
//
//        return candidateDocumentRepository
//                .findByStatus(DocumentStatus.PENDING)
//                .stream()
//                .map(this::mapToDto)
//                .toList();
//    }
//
//    //get dashboard statistics for HR
//    @Override
//    public DashboardStatisticsDto getDashboardStatistics() {
//
//        return DashboardStatisticsDto.builder()
//
//                .totalDocuments(
//                        candidateDocumentRepository.count()
//                )
//
//                .verified(
//                        candidateDocumentRepository
//                                .countByStatus(
//                                        DocumentStatus.VERIFIED
//                                )
//                )
//
//                .pending(
//                        candidateDocumentRepository
//                                .countByStatus(
//                                        DocumentStatus.PENDING
//                                )
//                )
//
//                .rejected(
//                        candidateDocumentRepository
//                                .countByStatus(
//                                        DocumentStatus.REJECTED
//                                )
//                )
//
//                .build();
//    }
//
////get candidate dashboard for HR
//    @Override
//    public List<CandidateDashboardDto>
//    getCandidateDashboard() {
//
//        List<Candidate> candidates =
//                candidateRepository.findAll();
//
//        List<CandidateDashboardDto> dashboard =
//                new ArrayList<>();
//
//        for (Candidate candidate : candidates) {
//
//            List<CandidateDocument> documents =
//                    candidateDocumentRepository
//                            .findByCandidate(candidate);
//
//            long verified =
//                    documents.stream()
//                            .filter(doc ->
//                                    doc.getStatus() ==
//                                            DocumentStatus.VERIFIED)
//                            .count();
//
//            long pending =
//                    documents.stream()
//                            .filter(doc ->
//                                    doc.getStatus() ==
//                                            DocumentStatus.PENDING)
//                            .count();
//
//            long rejected =
//                    documents.stream()
//                            .filter(doc ->
//                                    doc.getStatus() ==
//                                            DocumentStatus.REJECTED)
//                            .count();
//
//            dashboard.add(
//
//                    CandidateDashboardDto.builder()
//
//                            .candidateId(
//                                    candidate.getId()
//                            )
//
//                            .candidateName(
//                                    candidate.getUsername()
//                            )
//
//                            .email(
//                                    candidate.getEmail()
//                            )
//
//                            .phoneNumber(
//                                    candidate.getPhoneNumber()
//                            )
//
////                            .appliedRole(
////                                    candidate.getAppliedRole()
////                            )
//
//                            .candidateType(
//                                    candidate.getCandidateType()
//                            )
//
//                            .totalDocuments(
//                                    documents.size()
//                            )
//
//                            .verifiedDocuments(
//                                    verified
//                            )
//
//                            .pendingDocuments(
//                                    pending
//                            )
//
//                            .rejectedDocuments(
//                                    rejected
//                            )
//
//                            .build()
//            );
//        }
//
//        return dashboard;
//    }
//
//    //verify UAN for a candidate by HR
//    @Override
//    public void verifyUan(
//            Long candidateId
//    ) {
//
//        Candidate candidate =
//                candidateRepository
//                        .findById(candidateId)
//                        .orElseThrow(() ->
//                                new ResourceNotFoundException(
//                                        "Candidate",
//                                        candidateId
//                                )
//                        );
//
//        Employment employment =
//                candidate.getEmployment();
//
//        if (employment == null) {
//
//            throw new BadRequestException(
//                    "Employment details not found."
//            );
//        }
//
//        employment.setUanVerificationStatus(
//                VerificationStatus.VERIFIED
//        );
//
//        candidateRepository.save(candidate);
//
//        log.info(
//                "UAN verified successfully for candidate {}",
//                candidateId
//        );
//    }
//
//    //get all verification requests for HR dashboard
//    @Override
//    public List<HrVerificationRequestDto>
//    getAllVerificationRequests() {
//
//        return candidateRepository
//                .findAll()
//                .stream()
//
//                // Only candidates having documents
//                .filter(candidate ->
//                        candidateDocumentRepository
//                                .countByCandidate(candidate) > 0
//                )
//
//                .map(candidate -> {
//
//                    List<CandidateDocument> documents =
//                            candidateDocumentRepository
//                                    .findByCandidate(candidate);
//
//                    int count =
//                            documents.size();
//
//                  //calculate overall status based on individual document statuses
//                    DocumentStatus overallStatus;
//
//                    boolean rejected =
//                            documents.stream()
//                                    .anyMatch(doc ->
//                                            doc.getStatus() ==
//                                                    DocumentStatus.REJECTED
//                                    );
//
//                    boolean pending =
//                            documents.stream()
//                                    .anyMatch(doc ->
//                                            doc.getStatus() ==
//                                                    DocumentStatus.PENDING
//                                    );
//
//                    if (rejected) {
//
//                        overallStatus =
//                                DocumentStatus.REJECTED;
//
//                    } else if (pending) {
//
//                        overallStatus =
//                                DocumentStatus.PENDING;
//
//                    } else {
//
//                        overallStatus =
//                                DocumentStatus.VERIFIED;
//                    }
//
//                    //uan verification status
//                    VerificationStatus uanStatus =
//                            VerificationStatus.PENDING;
//
//                    if (candidate.getEmployment() != null &&
//                            candidate.getEmployment()
//                                    .getUanVerificationStatus() != null) {
//
//                        uanStatus =
//                                candidate.getEmployment()
//                                        .getUanVerificationStatus();
//                    }
//
//                    return HrVerificationRequestDto.builder()
//
//                            .candidateId(
//                                    candidate.getId()
//                            )
//
//                            .candidateName(
//                                    candidate.getUsername()
//                            )
//
//                            .email(
//                                    candidate.getEmail()
//                            )
//
//                            .candidateType(
//                                    candidate.getCandidateType()
//                            )
//
//                            .documentCount(
//                                    count
//                            )
//
//                            .uanVerificationStatus(
//                                    uanStatus
//                            )
//
//                            .status(
//                                    overallStatus
//                            )
//
//                            .build();
//                })
//
//                .toList();
//    }
//
//    //update application status based on document statuses
//    private void updateApplicationStatus(
//            Candidate candidate
//    ) {
//
//        List<CandidateDocument> documents =
//                candidateDocumentRepository
//                        .findByCandidate(candidate);
//
//        if (documents.isEmpty()) {
//
//            candidate.setApplicationStatus(
//                    ApplicationStatus.PENDING_VERIFICATION
//            );
//
//            candidateRepository.save(candidate);
//
//            return;
//        }
//
////any
//        boolean rejected =
//                documents.stream()
//                        .anyMatch(document ->
//                                document.getStatus() ==
//                                        DocumentStatus.REJECTED
//                        );
//
////any pending
//        boolean pending =
//                documents.stream()
//                        .anyMatch(document ->
//                                document.getStatus() ==
//                                        DocumentStatus.PENDING
//                        );
//
//    //application status logic based on document statuses
//        if (rejected) {
//
//            candidate.setApplicationStatus(
//                    ApplicationStatus.RE_UPLOAD_REQUIRED
//            );
//
//        } else if (pending) {
//
//            candidate.setApplicationStatus(
//                    ApplicationStatus.PENDING_VERIFICATION
//            );
//
//        } else {
//
//            candidate.setApplicationStatus(
//                    ApplicationStatus.DOCUMENTS_VERIFIED
//            );
//        }
//
//        candidateRepository.save(candidate);
//    }
//}