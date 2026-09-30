package com.verify_x.serviceImpl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.verify_x.dto.OfferLetterRequestDTO;
import com.verify_x.dto.OfferLetterResponseDTO;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.OfferLetter;
import com.verify_x.enums.InterviewStatus;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.OfferLetterRepository;
import com.verify_x.services.EmailService;
import com.verify_x.services.OfferLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferLetterServiceImpl
        implements OfferLetterService {

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;

    /*
     * Existing uploaded document types.
     */
    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            );

    /*
     * Default values according to the provided offer-letter format.
     */
    private static final BigDecimal DEFAULT_MONTHLY_STIPEND =
            new BigDecimal("10000");

    private static final BigDecimal DEFAULT_ANNUAL_SALARY =
            new BigDecimal("300000");

    private static final String COMPANY_NAME =
            "HourlyRecruit";

    private static final String HR_NAME =
            "Bharath Kumar R";

    private static final String HR_DESIGNATION =
            "HR Director – HourlyRecruit";

    private static final String HR_EMAIL =
            "Hr@hourlyrecruit.com";

    private static final String COMPANY_ADDRESS =
            "No 51, Old site No.1, 5th Floor, 5th Main road, "
                    + "above Alchemy Coffee Roasters, 36th Cross Rd, "
                    + "5th Block, Jayanagar, Bengaluru, Karnataka 560041.";

    private final OfferLetterRepository offerLetterRepository;

    private final CandidateRepository candidateRepository;

    private final EmailService emailService;


    // ============================================================
    // EXISTING CREATE OFFER LETTER
    // ============================================================

    @Override
    public OfferLetterResponseDTO createOfferLetter(
            Long candidateId,
            OfferLetterRequestDTO request,
            MultipartFile file
    ) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate not found with id: "
                                                + candidateId
                                )
                        );

        validateDates(request);

        validateFile(file);

        OfferLetter offerLetter =
                OfferLetter.builder()
                        .candidate(candidate)
                        .companyName(
                                request.getCompanyName().trim()
                        )
                        .designation(
                                cleanValue(request.getDesignation())
                        )
                        .ctc(request.getCtc())
                        .monthlyStipend(
                                request.getMonthlyStipend()
                        )
                        .annualSalary(
                                request.getAnnualSalary()
                        )
                        .offerDate(request.getOfferDate())
                        .joiningDate(request.getJoiningDate())
                        .referenceNumber(
                                cleanValue(
                                        request.getReferenceNumber()
                                )
                        )
                        .released(false)
                        .build();

        if (file != null && !file.isEmpty()) {
            storeFile(offerLetter, file);
        }

        OfferLetter saved =
                offerLetterRepository.save(offerLetter);

        return mapToResponse(saved);
    }


    // ============================================================
    // GET ALL OFFER LETTERS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<OfferLetterResponseDTO> getOfferLetters(
            Long candidateId
    ) {

        validateCandidateExists(candidateId);

        return offerLetterRepository
                .findByCandidateIdOrderByCreatedAtDesc(candidateId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ============================================================
    // GET SINGLE OFFER LETTER
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public OfferLetterResponseDTO getOfferLetter(
            Long candidateId,
            Long offerLetterId
    ) {

        OfferLetter offerLetter =
                getOfferLetterEntity(
                        candidateId,
                        offerLetterId
                );

        return mapToResponse(offerLetter);
    }


    // ============================================================
    // UPDATE OFFER LETTER
    // ============================================================

    @Override
    public OfferLetterResponseDTO updateOfferLetter(
            Long candidateId,
            Long offerLetterId,
            OfferLetterRequestDTO request,
            MultipartFile file
    ) {

        OfferLetter offerLetter =
                getOfferLetterEntity(
                        candidateId,
                        offerLetterId
                );

        validateDates(request);

        offerLetter.setCompanyName(
                request.getCompanyName().trim()
        );

        offerLetter.setDesignation(
                cleanValue(request.getDesignation())
        );

        offerLetter.setCtc(
                request.getCtc()
        );

        offerLetter.setMonthlyStipend(
                request.getMonthlyStipend()
        );

        offerLetter.setAnnualSalary(
                request.getAnnualSalary()
        );

        offerLetter.setOfferDate(
                request.getOfferDate()
        );

        offerLetter.setJoiningDate(
                request.getJoiningDate()
        );

        offerLetter.setReferenceNumber(
                cleanValue(
                        request.getReferenceNumber()
                )
        );

        // File is optional during update
        if (file != null && !file.isEmpty()) {
            validateFile(file);
            storeFile(offerLetter, file);
        }

        OfferLetter updated =
                offerLetterRepository.save(offerLetter);

        return mapToResponse(updated);
    }


    // ============================================================
    // DELETE OFFER LETTER
    // ============================================================

    @Override
    public void deleteOfferLetter(
            Long candidateId,
            Long offerLetterId
    ) {

        OfferLetter offerLetter =
                getOfferLetterEntity(
                        candidateId,
                        offerLetterId
                );

        offerLetterRepository.delete(offerLetter);
    }


    // ============================================================
    // DOWNLOAD DOCUMENT
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload getDocument(
            Long candidateId,
            Long offerLetterId
    ) {

        OfferLetter offerLetter =
                getOfferLetterEntity(
                        candidateId,
                        offerLetterId
                );

        if (offerLetter.getDocumentData() == null
                || offerLetter.getDocumentData().length == 0) {

            throw new ResourceNotFoundException(
                    "No document uploaded for offer letter id: "
                            + offerLetterId
            );
        }

        return new DocumentDownload(
                offerLetter.getDocumentData(),
                offerLetter.getDocumentFileName(),
                offerLetter.getDocumentContentType()
        );
    }


    // ============================================================
    // RELEASE OFFER LETTER
    // ============================================================

    @Override
    public OfferLetterResponseDTO releaseOfferLetter(
            Long candidateId,
            OfferLetterRequestDTO request
    ) {

        /*
         * 1. Find candidate
         */
        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate not found with id: "
                                                + candidateId
                                )
                        );


        /*
         * 2. Check HR interview status
         *
         * Only candidates whose HR interview was APPROVED
         * can receive an offer letter.
         */
        if (candidate.getInterviewStatus()
                != InterviewStatus.APPROVED) {

            throw new IllegalStateException(
                    "Offer letter cannot be released. "
                            + "Candidate HR interview is not approved."
            );
        }


        /*
         * 3. Validate dates
         */
        validateDates(request);


        /*
         * 4. Prevent duplicate release
         */
        List<OfferLetter> existingOfferLetters =
                offerLetterRepository
                        .findByCandidateIdOrderByCreatedAtDesc(
                                candidateId
                        );

        boolean alreadyReleased =
                existingOfferLetters
                        .stream()
                        .anyMatch(OfferLetter::isReleased);

        if (alreadyReleased) {

            throw new IllegalStateException(
                    "Offer letter has already been released "
                            + "for candidate id: "
                            + candidateId
            );
        }


        /*
         * 5. Use requested values if provided.
         *
         * Otherwise use the values from the
         * provided HourlyRecruit offer-letter format.
         */
        BigDecimal monthlyStipend =
                request.getMonthlyStipend() != null
                        ? request.getMonthlyStipend()
                        : DEFAULT_MONTHLY_STIPEND;

        BigDecimal annualSalary =
                request.getAnnualSalary() != null
                        ? request.getAnnualSalary()
                        : DEFAULT_ANNUAL_SALARY;


        /*
         * 6. Determine designation.
         *
         * If HR provides designation, use it.
         * Otherwise use candidate's applied role.
         */
        String designation =
                cleanValue(request.getDesignation());

        if (designation == null) {

            if (candidate.getAppliedRole() != null) {

                designation =
                        formatEnumValue(
                                candidate.getAppliedRole().name()
                        );

            } else {

                designation =
                        "Junior Software Engineer";
            }
        }


        /*
         * 7. Generate PDF
         */
        byte[] pdfData =
                generateOfferLetterPdf(
                        candidate,
                        designation,
                        monthlyStipend,
                        annualSalary,
                        request.getOfferDate(),
                        request.getJoiningDate()
                );


        /*
         * 8. Create OfferLetter entity
         */
        OfferLetter offerLetter =
                OfferLetter.builder()
                        .candidate(candidate)
                        .companyName(
                                cleanValue(
                                        request.getCompanyName()
                                ) != null
                                        ? request.getCompanyName().trim()
                                        : COMPANY_NAME
                        )
                        .designation(designation)
                        .ctc(
                                request.getCtc() != null
                                        ? request.getCtc()
                                        : new BigDecimal("3.00")
                        )
                        .monthlyStipend(monthlyStipend)
                        .annualSalary(annualSalary)
                        .offerDate(request.getOfferDate())
                        .joiningDate(request.getJoiningDate())
                        .referenceNumber(
                                cleanValue(
                                        request.getReferenceNumber()
                                )
                        )
                        .released(true)
                        .releasedAt(LocalDateTime.now())
                        .documentFileName(
                                createOfferFileName(candidate)
                        )
                        .documentContentType(
                                "application/pdf"
                        )
                        .documentSize(
                                (long) pdfData.length
                        )
                        .documentData(pdfData)
                        .build();


        /*
         * 9. Save PDF + offer details to database
         */
        OfferLetter saved =
                offerLetterRepository.save(offerLetter);


        /*
         * 10. Send offer letter by email
         */
        String candidateName =
                cleanValue(candidate.getUsername());

        if (candidateName == null) {
            candidateName = "Candidate";
        }

        emailService.sendOfferLetterEmail(
                candidate.getEmail(),
                candidateName,
                pdfData,
                saved.getDocumentFileName()
        );


        /*
         * 11. Return response
         */
        return mapToResponse(saved);
    }


    // ============================================================
    // PDF GENERATION
    // ============================================================

    private byte[] generateOfferLetterPdf(
            Candidate candidate,
            String designation,
            BigDecimal monthlyStipend,
            BigDecimal annualSalary,
            LocalDate offerDate,
            LocalDate joiningDate
    ) {

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(
                            PageSize.A4,
                            50,
                            50,
                            50,
                            50
                    );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();


            /*
             * Fonts
             */
            Font companyFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Font.BOLD
                    );

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            15,
                            Font.BOLD
                    );

            Font normalFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            10,
                            Font.NORMAL
                    );

            Font boldFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10,
                            Font.BOLD
                    );


            /*
             * Company name
             */
            Paragraph company =
                    new Paragraph(
                            COMPANY_NAME,
                            companyFont
                    );

            company.setAlignment(Element.ALIGN_CENTER);

            document.add(company);


            /*
             * Company address
             */
            Paragraph address =
                    new Paragraph(
                            COMPANY_ADDRESS,
                            normalFont
                    );

            address.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(address);

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Title
             */
            Paragraph title =
                    new Paragraph(
                            "OFFER LETTER",
                            titleFont
                    );

            title.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(title);

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Date
             */
            String formattedOfferDate =
                    offerDate.format(
                            DateTimeFormatter.ofPattern(
                                    "dd'th' MMMM yyyy"
                            )
                    ).toUpperCase();

            document.add(
                    new Paragraph(
                            "Date: "
                                    + formattedOfferDate,
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Candidate address
             */
            document.add(
                    new Paragraph(
                            "To:",
                            boldFont
                    )
            );

            String candidateName =
                    cleanValue(
                            candidate.getUsername()
                    );

            if (candidateName == null) {
                candidateName = "Candidate";
            }

            document.add(
                    new Paragraph(
                            candidateName,
                            normalFont
                    )
            );


            String candidateAddress =
                    cleanValue(
                            candidate.getAddress()
                    );

            if (candidateAddress != null) {

                document.add(
                        new Paragraph(
                                candidateAddress,
                                normalFont
                        )
                );
            }

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Greeting
             */
            document.add(
                    new Paragraph(
                            "Dear "
                                    + candidateName
                                    + ",",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Opening paragraph
             */
            document.add(
                    new Paragraph(
                            "We are pleased to offer you the position of "
                                    + designation
                                    + " at "
                                    + COMPANY_NAME
                                    + ". "
                                    + "We are delighted to have you join our team.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Compensation
             */
            Paragraph compensation =
                    new Paragraph();

            compensation.add(
                    new Chunk(
                            "Stipend: ",
                            boldFont
                    )
            );

            compensation.add(
                    new Chunk(
                            "₹"
                                    + formatAmount(
                                    monthlyStipend
                            )
                                    + " monthly during the probation period "
                                    + "of approximately 2–3 months, "
                                    + "with PF benefits as per company policy.",
                            normalFont
                    )
            );

            document.add(compensation);

            document.add(
                    new Paragraph(" ")
            );


            Paragraph salary =
                    new Paragraph();

            salary.add(
                    new Chunk(
                            "Salary: ",
                            boldFont
                    )
            );

            salary.add(
                    new Chunk(
                            "Up to ₹"
                                    + formatAmount(
                                    annualSalary
                            )
                                    + " annually ("
                                    + formatLpa(
                                    annualSalary
                            )
                                    + " LPA) after successful completion "
                                    + "of the probation period.",
                            normalFont
                    )
            );

            document.add(salary);

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Employment terms
             */
            document.add(
                    new Paragraph(
                            "Employment Terms:",
                            boldFont
                    )
            );

            document.add(
                    new Paragraph(
                            "• This offer is contingent upon successful "
                                    + "completion of onboarding documentation "
                                    + "and background verification.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "• You are expected to follow company policies, "
                                    + "code of conduct and confidentiality requirements.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "• Your work location, project assignment and "
                                    + "shift may be determined based on business requirements.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Joining date
             */
            String formattedJoiningDate =
                    joiningDate.format(
                            DateTimeFormatter.ofPattern(
                                    "dd MMMM yyyy"
                            )
                    );

            document.add(
                    new Paragraph(
                            "Joining Date: "
                                    + formattedJoiningDate,
                            boldFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );


            /*
             * Acknowledgement
             */
            document.add(
                    new Paragraph(
                            "Please acknowledge your acceptance of this "
                                    + "offer by email within 48 hours of receiving "
                                    + "this letter.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "We look forward to welcoming you to "
                                    + COMPANY_NAME
                                    + ".",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "Regards,",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            HR_NAME,
                            boldFont
                    )
            );

            document.add(
                    new Paragraph(
                            HR_DESIGNATION,
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            HR_EMAIL,
                            normalFont
                    )
            );

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to generate offer letter PDF",
                    e
            );
        }
    }


    // ============================================================
    // HELPER - OFFER FILE NAME
    // ============================================================

    private String createOfferFileName(
            Candidate candidate
    ) {

        String candidateName =
                cleanValue(
                        candidate.getUsername()
                );

        if (candidateName == null) {
            candidateName = "Candidate";
        }

        return sanitizeFileName(
                "Offer_Letter_"
                        + candidateName
                        + ".pdf"
        );
    }


    // ============================================================
    // HELPER - FORMAT MONEY
    // ============================================================

    private String formatAmount(
            BigDecimal amount
    ) {

        if (amount == null) {
            return "0";
        }

        return amount.stripTrailingZeros()
                .toPlainString();
    }


    // ============================================================
    // HELPER - FORMAT LPA
    // ============================================================

    private String formatLpa(
            BigDecimal annualSalary
    ) {

        if (annualSalary == null) {
            return "0";
        }

        return annualSalary
                .divide(
                        new BigDecimal("100000")
                )
                .stripTrailingZeros()
                .toPlainString();
    }


    // ============================================================
    // HELPER - ENUM FORMAT
    // ============================================================

    private String formatEnumValue(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String[] words =
                value.toLowerCase()
                        .split("_");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {
                result.append(
                        word.substring(1)
                );
            }

            result.append(" ");
        }

        return result.toString().trim();
    }


    // ============================================================
    // EXISTING HELPER - GET OFFER LETTER
    // ============================================================

    private OfferLetter getOfferLetterEntity(
            Long candidateId,
            Long offerLetterId
    ) {

        return offerLetterRepository
                .findByIdAndCandidateId(
                        offerLetterId,
                        candidateId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Offer letter not found with id: "
                                        + offerLetterId
                                        + " for candidate: "
                                        + candidateId
                        )
                );
    }


    // ============================================================
    // EXISTING HELPER - VALIDATE CANDIDATE
    // ============================================================

    private void validateCandidateExists(
            Long candidateId
    ) {

        if (!candidateRepository.existsById(candidateId)) {

            throw new ResourceNotFoundException(
                    "Candidate not found with id: "
                            + candidateId
            );
        }
    }


    // ============================================================
    // EXISTING HELPER - VALIDATE DATES
    // ============================================================

    private void validateDates(
            OfferLetterRequestDTO request
    ) {

        if (request.getOfferDate() != null
                && request.getJoiningDate() != null
                && request.getJoiningDate()
                .isBefore(request.getOfferDate())) {

            throw new IllegalArgumentException(
                    "Joining date cannot be before offer date"
            );
        }
    }


    // ============================================================
    // EXISTING HELPER - VALIDATE FILE
    // ============================================================

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Offer letter document is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "File size cannot exceed 5 MB"
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                contentType.toLowerCase()
        )) {

            throw new IllegalArgumentException(
                    "Only PDF, JPG and PNG files are allowed"
            );
        }
    }


    // ============================================================
    // EXISTING HELPER - STORE FILE
    // ============================================================

    private void storeFile(
            OfferLetter offerLetter,
            MultipartFile file
    ) {

        try {

            offerLetter.setDocumentData(
                    file.getBytes()
            );

            offerLetter.setDocumentFileName(
                    sanitizeFileName(
                            file.getOriginalFilename()
                    )
            );

            offerLetter.setDocumentContentType(
                    file.getContentType()
            );

            offerLetter.setDocumentSize(
                    file.getSize()
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to read offer letter document",
                    e
            );
        }
    }


    // ============================================================
    // MAP ENTITY TO RESPONSE DTO
    // ============================================================

    private OfferLetterResponseDTO mapToResponse(
            OfferLetter offerLetter
    ) {

        return OfferLetterResponseDTO.builder()
                .id(offerLetter.getId())
                .candidateId(
                        offerLetter.getCandidate().getId()
                )
                .companyName(
                        offerLetter.getCompanyName()
                )
                .designation(
                        offerLetter.getDesignation()
                )
                .ctc(
                        offerLetter.getCtc()
                )
                .monthlyStipend(
                        offerLetter.getMonthlyStipend()
                )
                .annualSalary(
                        offerLetter.getAnnualSalary()
                )
                .offerDate(
                        offerLetter.getOfferDate()
                )
                .joiningDate(
                        offerLetter.getJoiningDate()
                )
                .referenceNumber(
                        offerLetter.getReferenceNumber()
                )
                .documentFileName(
                        offerLetter.getDocumentFileName()
                )
                .documentContentType(
                        offerLetter.getDocumentContentType()
                )
                .documentSize(
                        offerLetter.getDocumentSize()
                )
                .documentAvailable(
                        offerLetter.getDocumentData() != null
                                && offerLetter.getDocumentData().length > 0
                )
                .released(
                        offerLetter.isReleased()
                )
                .releasedAt(
                        offerLetter.getReleasedAt()
                )
                .createdAt(
                        offerLetter.getCreatedAt()
                )
                .updatedAt(
                        offerLetter.getUpdatedAt()
                )
                .build();
    }


    // ============================================================
    // CLEAN VALUE
    // ============================================================

    private String cleanValue(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    // ============================================================
    // SANITIZE FILE NAME
    // ============================================================

    private String sanitizeFileName(
            String fileName
    ) {

        if (fileName == null || fileName.isBlank()) {
            return "offer-letter";
        }

        return fileName
                .replace("\\", "_")
                .replace("/", "_")
                .replace("..", "_");
    }
}