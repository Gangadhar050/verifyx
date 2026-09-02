//package com.verify_x.serviceImpl;

package com.verify_x.serviceImpl;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Education;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EducationRepository;
import com.verify_x.services.EducationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final CandidateRepository candidateRepository;
    private final com.verify_x.util.GeminiEducationParser geminiEducationParser;
    private final com.verify_x.util.EducationPdfTextExtractor educationPdfTextExtractor;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        assert principal != null;
        return candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));
    }

    private EducationResponse mapToResponse(Education education) {

        return EducationResponse.builder()
                .id(education.getId())
                .tenthSchoolName(education.getTenthSchoolName())
                .tenthBoard(education.getTenthBoard())
                .tenthSchoolLocation(education.getTenthSchoolLocation())
                .tenthRollNumber(education.getTenthRollNumber())
                .tenthPassingYear(education.getTenthPassingYear())
                .tenthPercentage(education.getTenthPercentage())
                .tenthMarksCardName(education.getTenthMarksCardName())

                .twelfthInstitutionName(education.getTwelfthInstitutionName())
                .twelfthBoardUniversity(education.getTwelfthBoardUniversity())
                .twelfthLocation(education.getTwelfthLocation())
                .twelfthRegistrationNumber(education.getTwelfthRegistrationNumber())
                .twelfthPassingYear(education.getTwelfthPassingYear())
                .twelfthPercentage(education.getTwelfthPercentage())
                .twelfthMarksCardName(education.getTwelfthMarksCardName())

                .degreeName(education.getDegreeName())
                .specialization(education.getSpecialization())
                .collegeName(education.getCollegeName())
                .universityName(education.getUniversityName())
                .usnNumber(education.getUsnNumber())
                .degreeLocation(education.getDegreeLocation())
                .degreeStartYear(education.getDegreeStartYear())
                .degreeEndYear(education.getDegreeEndYear())
                .degreePercentage(education.getDegreePercentage())
                .degreeCertificateName(education.getDegreeCertificateName())

                .mastersDegree(education.getMastersDegree())
                .mastersSpecialization(education.getMastersSpecialization())
                .mastersCollege(education.getMastersCollege())
                .mastersUniversity(education.getMastersUniversity())
                .mastersRegistrationNumber(education.getMastersRegistrationNumber())
                .mastersLocation(education.getMastersLocation())
                .mastersStartYear(education.getMastersStartYear())
                .mastersEndYear(education.getMastersEndYear())
                .mastersPercentage(education.getMastersPercentage())
                .mastersMarksCardName(education.getMastersMarksCardName())
                .build();
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Maximum allowed file size is 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only PDF, JPG, JPEG and PNG files are allowed.");
        }

        log.debug("Validated education document: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());
    }

    private void saveFile(
            MultipartFile file,
            Consumer<byte[]> dataSetter,
            Consumer<String> fileNameSetter,
            Consumer<String> contentTypeSetter) throws IOException {

        if (file == null || file.isEmpty()) {
            return;
        }

        validateFile(file);

        dataSetter.accept(file.getBytes());
        fileNameSetter.accept(file.getOriginalFilename());
        contentTypeSetter.accept(file.getContentType());
    }

    private void mapRequestToEntity(
            Education education,
            EducationRequest request) throws IOException {

        education.setTenthSchoolName(request.getTenthSchoolName());
        education.setTenthBoard(request.getTenthBoard());
        education.setTenthSchoolLocation(request.getTenthSchoolLocation());
        education.setTenthRollNumber(request.getTenthRollNumber());
        education.setTenthPassingYear(request.getTenthPassingYear());
        education.setTenthPercentage(request.getTenthPercentage());

        education.setTwelfthInstitutionName(request.getTwelfthInstitutionName());
        education.setTwelfthBoardUniversity(request.getTwelfthBoardUniversity());
        education.setTwelfthLocation(request.getTwelfthLocation());
        education.setTwelfthRegistrationNumber(request.getTwelfthRegistrationNumber());
        education.setTwelfthPassingYear(request.getTwelfthPassingYear());
        education.setTwelfthPercentage(request.getTwelfthPercentage());

        education.setDegreeName(request.getDegreeName());
        education.setSpecialization(request.getSpecialization());
        education.setCollegeName(request.getCollegeName());
        education.setUniversityName(request.getUniversityName());
        education.setDegreeLocation(request.getDegreeLocation());
        education.setUsnNumber(request.getUsnNumber());
        education.setDegreeStartYear(request.getDegreeStartYear());
        education.setDegreeEndYear(request.getDegreeEndYear());
        education.setDegreePercentage(request.getDegreePercentage());

        education.setMastersDegree(request.getMastersDegree());
        education.setMastersSpecialization(request.getMastersSpecialization());
        education.setMastersCollege(request.getMastersCollege());
        education.setMastersUniversity(request.getMastersUniversity());
        education.setMastersRegistrationNumber(request.getMastersRegistrationNumber());
        education.setMastersLocation(request.getMastersLocation());
        education.setMastersStartYear(request.getMastersStartYear());
        education.setMastersEndYear(request.getMastersEndYear());
        education.setMastersPercentage(request.getMastersPercentage());

        saveFile(
                request.getTenthMarksCard(),
                education::setTenthMarksCard,
                education::setTenthMarksCardName,
                education::setTenthMarksCardContentType
        );

        saveFile(
                request.getTwelfthMarksCard(),
                education::setTwelfthMarksCard,
                education::setTwelfthMarksCardName,
                education::setTwelfthMarksCardContentType
        );

        saveFile(
                request.getDegreeCertificate(),
                education::setDegreeCertificate,
                education::setDegreeCertificateName,
                education::setDegreeCertificateContentType
        );

        saveFile(
                request.getMastersMarksCard(),
                education::setMastersMarksCard,
                education::setMastersMarksCardName,
                education::setMastersMarksCardContentType
        );
    }

    public EducationResponse autoFillFromDocument(
            MultipartFile file, EducationDocumentType documentType, EducationRequest request) {

        validateFile(file);

        Map<String, String> fields = new java.util.LinkedHashMap<>(
                educationPdfTextExtractor.extract(file, documentType)
        );

        try {
            String geminiJson = geminiEducationParser.parseEducationDocument(file, documentType);
            Map<String, String> aiFields = parseJsonSafely(geminiJson);
            aiFields.forEach((key, value) -> {
                if (value != null && !value.isBlank()) fields.put(key, value);
            });
        } catch (RuntimeException aiError) {
            log.warn("Cloud extraction unavailable for {}: {}", documentType, aiError.getMessage());
        }

        if (fields.isEmpty()) {
            throw new BadRequestException(
                    "The document could not be read automatically. For scanned PDF/JPG/PNG files, "
                            + "configure GEMINI_API_KEY in the backend and restart it. "
                            + "Text-based PDFs can be read locally without Gemini."
            );
        }

        Candidate candidate = getLoggedInCandidate();

        Education preview = educationRepository.findByCandidate(candidate)
                .map(this::copyOf)
                .orElseGet(() -> {
                    Education e = new Education();
                    e.setCandidate(candidate);
                    return e;
                });

        applyExtractedFields(preview, documentType, fields);

        log.info("Preview auto-fill for {} generated for candidate {} (not saved)", documentType, candidate.getId());

        return mapToResponse(preview);
    }

    private Education copyOf(Education source) {
        Education copy = new Education();
        copy.setId(source.getId());
        copy.setCandidate(source.getCandidate());

        copy.setTenthSchoolName(source.getTenthSchoolName());
        copy.setTenthBoard(source.getTenthBoard());
        copy.setTenthSchoolLocation(source.getTenthSchoolLocation());
        copy.setTenthRollNumber(source.getTenthRollNumber());
        copy.setTenthPassingYear(source.getTenthPassingYear());
        copy.setTenthPercentage(source.getTenthPercentage());
        copy.setTenthMarksCardName(source.getTenthMarksCardName());

        copy.setTwelfthInstitutionName(source.getTwelfthInstitutionName());
        copy.setTwelfthBoardUniversity(source.getTwelfthBoardUniversity());
        copy.setTwelfthRegistrationNumber(source.getTwelfthRegistrationNumber());
        copy.setTwelfthPassingYear(source.getTwelfthPassingYear());
        copy.setTwelfthPercentage(source.getTwelfthPercentage());
        copy.setTwelfthMarksCardName(source.getTwelfthMarksCardName());

        copy.setDegreeName(source.getDegreeName());
        copy.setSpecialization(source.getSpecialization());
        copy.setCollegeName(source.getCollegeName());
        copy.setUniversityName(source.getUniversityName());
        copy.setUsnNumber(source.getUsnNumber());
        copy.setDegreeStartYear(source.getDegreeStartYear());
        copy.setDegreeEndYear(source.getDegreeEndYear());
        copy.setDegreePercentage(source.getDegreePercentage());
        copy.setDegreeCertificateName(source.getDegreeCertificateName());

        copy.setMastersDegree(source.getMastersDegree());
        copy.setMastersSpecialization(source.getMastersSpecialization());
        copy.setMastersCollege(source.getMastersCollege());
        copy.setMastersUniversity(source.getMastersUniversity());
        copy.setMastersRegistrationNumber(source.getMastersRegistrationNumber());
        copy.setMastersStartYear(source.getMastersStartYear());
        copy.setMastersEndYear(source.getMastersEndYear());
        copy.setMastersPercentage(source.getMastersPercentage());
        copy.setMastersMarksCardName(source.getMastersMarksCardName());

        return copy;
    }

    private Map<String, String> parseJsonSafely(String geminiResponse) {
        if (geminiResponse == null || geminiResponse.isBlank()) return new java.util.LinkedHashMap<>();
        int start = geminiResponse.indexOf('{');
        int end = geminiResponse.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) return new java.util.LinkedHashMap<>();
        try {
            return objectMapper.readValue(
                    geminiResponse.substring(start, end + 1),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.LinkedHashMap<String, String>>() {
                    });
        } catch (Exception e) {
            log.error("Failed to parse Gemini JSON: {}", geminiResponse, e);
            return new java.util.LinkedHashMap<>();
        }
    }

    private void applyExtractedFields(
            Education education, EducationDocumentType type, Map<String, String> f) {

        switch (type) {
            case TENTH_MARKS_CARD -> {
                setIfPresent(f, "tenthSchoolName", education::setTenthSchoolName);
                setIfPresent(f, "tenthBoard", education::setTenthBoard);
                setIfPresent(f, "tenthSchoolLocation", education::setTenthSchoolLocation);
                setIfPresent(f, "tenthRollNumber", education::setTenthRollNumber);
                setIntIfPresent(f, "tenthPassingYear", education::setTenthPassingYear);
                setDoubleIfPresent(f, "tenthPercentage", education::setTenthPercentage);
            }
            case TWELFTH_MARKS_CARD -> {
                setIfPresent(f, "twelfthInstitutionName", education::setTwelfthInstitutionName);
                setIfPresent(f, "twelfthBoardUniversity", education::setTwelfthBoardUniversity);
                setIfPresent(f, "twelfthLocation", education::setTwelfthLocation);
                setIfPresent(f, "twelfthRegistrationNumber", education::setTwelfthRegistrationNumber);
                setIntIfPresent(f, "twelfthPassingYear", education::setTwelfthPassingYear);
                setDoubleIfPresent(f, "twelfthPercentage", education::setTwelfthPercentage);
            }
            case DEGREE_CERTIFICATE -> {
                setIfPresent(f, "degreeName", education::setDegreeName);
                setIfPresent(f, "specialization", education::setSpecialization);
                setIfPresent(f, "collegeName", education::setCollegeName);
                setIfPresent(f, "universityName", education::setUniversityName);
                setIfPresent(f, "usnNumber", education::setUsnNumber);
                setIfPresent(f, "degreeLocation", education::setDegreeLocation);
                setIntIfPresent(f, "degreeStartYear", education::setDegreeStartYear);
                setIntIfPresent(f, "degreeEndYear", education::setDegreeEndYear);
                setDoubleIfPresent(f, "degreePercentage", education::setDegreePercentage);
            }
            case MASTERS_MARKS_CARD, MASTERS_DEGREE_CERTIFICATE -> {
                setIfPresent(f, "mastersDegree", education::setMastersDegree);
                setIfPresent(f, "mastersSpecialization", education::setMastersSpecialization);
                setIfPresent(f, "mastersCollege", education::setMastersCollege);
                setIfPresent(f, "mastersUniversity", education::setMastersUniversity);
                setIfPresent(f, "mastersLocation", education::setMastersLocation);
                setIfPresent(f, "mastersRegistrationNumber", education::setMastersRegistrationNumber);
                setIntIfPresent(f, "mastersStartYear", education::setMastersStartYear);
                setIntIfPresent(f, "mastersEndYear", education::setMastersEndYear);
                setDoubleIfPresent(f, "mastersPercentage", education::setMastersPercentage);
            }
        }
    }

    private void setIfPresent(Map<String, String> f, String key, Consumer<String> setter) {
        String v = f.get(key);
        if (v != null && !v.isBlank()) setter.accept(v.trim());
    }

    private void setIntIfPresent(Map<String, String> f, String key, Consumer<Integer> setter) {
        String v = f.get(key);
        if (v == null || v.isBlank()) return;
        try {
            setter.accept(Integer.parseInt(v.replaceAll("[^0-9]", "")));
        } catch (NumberFormatException e) {
            log.warn("Could not parse integer for {}: {}", key, v);
        }
    }

    private void setDoubleIfPresent(Map<String, String> f, String key, Consumer<Double> setter) {
        String v = f.get(key);
        if (v == null || v.isBlank()) return;
        try {
            setter.accept(Double.parseDouble(v.replaceAll("[^0-9.]", "")));
        } catch (NumberFormatException e) {
            log.warn("Could not parse double for {}: {}", key, v);
        }
    }

    private <E extends Enum<E>> void setEnumIfPresent(
            Map<String, String> f, String key, Consumer<E> setter, Class<E> enumClass) {
        String v = f.get(key);
        if (v == null || v.isBlank()) return;
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(v.trim().replace(" ", "_"))) {
                setter.accept(constant);
                return;
            }
        }
        log.warn("No matching enum constant for {} value '{}' - leaving field unset", key, v);
    }

    @Override
    public EducationResponse extractAllAndSave(
            MultipartFile tenthMarksCard,
            MultipartFile twelfthMarksCard,
            MultipartFile degreeCertificate,
            MultipartFile mastersMarksCard) {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository.findByCandidate(candidate)
                .orElseGet(() -> {
                    Education e = new Education();
                    e.setCandidate(candidate);
                    return e;
                });

        if (tenthMarksCard != null && !tenthMarksCard.isEmpty()) {
            processAndApply(education, tenthMarksCard, EducationDocumentType.TENTH_MARKS_CARD);
            try {
                saveFile(tenthMarksCard,
                        education::setTenthMarksCard,
                        education::setTenthMarksCardName,
                        education::setTenthMarksCardContentType);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save 10th marks card file.", e);
            }
        }

        if (twelfthMarksCard != null && !twelfthMarksCard.isEmpty()) {
            processAndApply(education, twelfthMarksCard, EducationDocumentType.TWELFTH_MARKS_CARD);
            try {
                saveFile(twelfthMarksCard,
                        education::setTwelfthMarksCard,
                        education::setTwelfthMarksCardName,
                        education::setTwelfthMarksCardContentType);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save 12th marks card file.", e);
            }
        }

        if (degreeCertificate != null && !degreeCertificate.isEmpty()) {
            processAndApply(education, degreeCertificate, EducationDocumentType.DEGREE_CERTIFICATE);
            try {
                saveFile(degreeCertificate,
                        education::setDegreeCertificate,
                        education::setDegreeCertificateName,
                        education::setDegreeCertificateContentType);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save degree certificate file.", e);
            }
        }

        if (mastersMarksCard != null && !mastersMarksCard.isEmpty()) {
            processAndApply(education, mastersMarksCard, EducationDocumentType.MASTERS_MARKS_CARD);
            try {
                saveFile(mastersMarksCard,
                        education::setMastersMarksCard,
                        education::setMastersMarksCardName,
                        education::setMastersMarksCardContentType);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save masters marks card file.", e);
            }
        }

        Education saved = educationRepository.save(education);

        log.info("Auto-extracted and saved education details for candidate {}", candidate.getId());

        return mapToResponse(saved);
    }

    private void processAndApply(Education education, MultipartFile file, EducationDocumentType type) {

        validateFile(file);

        String geminiJson = geminiEducationParser.parseEducationDocument(file, type);
        Map<String, String> fields = parseJsonSafely(geminiJson);

        applyExtractedFields(education, type, fields);
    }

    /**
     * Manual field update — used when the candidate edits fields by hand
     * via form input rather than uploading a new document.
     */
    @Override
    @Transactional
    public EducationResponse updateEducation(
            EducationRequest request,
            MultipartFile tenthMarksCard,
            MultipartFile twelfthMarksCard,
            MultipartFile degreeCertificate,
            MultipartFile mastersMarksCard) {

        try {

            Candidate candidate = getLoggedInCandidate();

            Education education = educationRepository
                    .findByCandidate(candidate)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Education details not found. Save first before updating."
                            )
                    );

            if (request == null) {
                throw new BadRequestException("Education details are required.");
            }

            updateEducationFields(education, request);

            if (tenthMarksCard != null && !tenthMarksCard.isEmpty()) {
                education.setTenthMarksCard(tenthMarksCard.getBytes());
            }

            if (twelfthMarksCard != null && !twelfthMarksCard.isEmpty()) {
                education.setTwelfthMarksCard(twelfthMarksCard.getBytes());
            }

            if (degreeCertificate != null && !degreeCertificate.isEmpty()) {
                education.setDegreeCertificate(degreeCertificate.getBytes());
            }

            if (mastersMarksCard != null && !mastersMarksCard.isEmpty()) {
                education.setMastersMarksCard(mastersMarksCard.getBytes());
            }

            Education updatedEducation = educationRepository.save(education);

            log.info("Education details updated for candidate {}", candidate.getId());

            return mapToResponse(updatedEducation);

        } catch (IOException ex) {
            log.error("Failed to update education details.", ex);
            throw new RuntimeException("Unable to update education details.", ex);
        }
    }

    /**
     * Document-driven update (NEW) — candidate uploads marksheet(s)/certificate(s),
     * Gemini/local extraction runs, and the extracted fields overwrite the existing
     * record's corresponding section. No manual EducationRequest needed.
     */
    @Override
    @Transactional
    public EducationResponse updateEducationFromDocuments(
            MultipartFile tenthMarksCard,
            MultipartFile twelfthMarksCard,
            MultipartFile degreeCertificate,
            MultipartFile mastersMarksCard) {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository.findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Education details not found. Save first before updating."));

        try {

            if (tenthMarksCard != null && !tenthMarksCard.isEmpty()) {
                processAndApply(education, tenthMarksCard, EducationDocumentType.TENTH_MARKS_CARD);
                saveFile(tenthMarksCard,
                        education::setTenthMarksCard,
                        education::setTenthMarksCardName,
                        education::setTenthMarksCardContentType);
            }

            if (twelfthMarksCard != null && !twelfthMarksCard.isEmpty()) {
                processAndApply(education, twelfthMarksCard, EducationDocumentType.TWELFTH_MARKS_CARD);
                saveFile(twelfthMarksCard,
                        education::setTwelfthMarksCard,
                        education::setTwelfthMarksCardName,
                        education::setTwelfthMarksCardContentType);
            }

            if (degreeCertificate != null && !degreeCertificate.isEmpty()) {
                processAndApply(education, degreeCertificate, EducationDocumentType.DEGREE_CERTIFICATE);
                saveFile(degreeCertificate,
                        education::setDegreeCertificate,
                        education::setDegreeCertificateName,
                        education::setDegreeCertificateContentType);
            }

            if (mastersMarksCard != null && !mastersMarksCard.isEmpty()) {
                processAndApply(education, mastersMarksCard, EducationDocumentType.MASTERS_MARKS_CARD);
                saveFile(mastersMarksCard,
                        education::setMastersMarksCard,
                        education::setMastersMarksCardName,
                        education::setMastersMarksCardContentType);
            }

        } catch (IOException ex) {
            log.error("Failed to update education details from documents.", ex);
            throw new RuntimeException("Unable to update education details from documents.", ex);
        }

        Education saved = educationRepository.save(education);

        log.info("Auto-extracted and updated education details from documents for candidate {}", candidate.getId());

        return mapToResponse(saved);
    }

    private void updateEducationFields(
            Education education,
            EducationRequest request) {

        if (request.getTenthPercentage() != null) {
            education.setTenthPercentage(request.getTenthPercentage());
        }

        if (request.getTwelfthPercentage() != null) {
            education.setTwelfthPercentage(request.getTwelfthPercentage());
        }

        if (request.getDegreePercentage() != null) {
            education.setDegreePercentage(request.getDegreePercentage());
        }

        if (request.getMastersPercentage() != null) {
            education.setMastersPercentage(request.getMastersPercentage());
        }

        if (request.getTenthPassingYear() != null) {
            education.setTenthPassingYear(request.getTenthPassingYear());
        }

        if (request.getTwelfthPassingYear() != null) {
            education.setTwelfthPassingYear(request.getTwelfthPassingYear());
        }

        if (request.getDegreeEndYear() != null) {
            education.setDegreeEndYear(request.getDegreeEndYear());
        }

        if (request.getMastersEndYear() != null) {
            education.setMastersEndYear(request.getMastersEndYear());
        }

        if (request.getTenthBoard() != null && !request.getTenthBoard().isBlank()) {
            education.setTenthBoard(request.getTenthBoard());
        }

        if (request.getTwelfthBoardUniversity() != null && !request.getTwelfthBoardUniversity().isBlank()) {
            education.setTwelfthBoardUniversity(request.getTwelfthBoardUniversity());
        }

        if (request.getUniversityName() != null && !request.getUniversityName().isBlank()) {
            education.setUniversityName(request.getUniversityName());
        }

        if (request.getDegreeName() != null && !request.getDegreeName().isBlank()) {
            education.setDegreeName(request.getDegreeName());
        }

        if (request.getMastersDegree() != null && !request.getMastersDegree().isBlank()) {
            education.setMastersDegree(request.getMastersDegree());
        }

        if (request.getMastersCollege() != null && !request.getMastersCollege().isBlank()) {
            education.setMastersCollege(request.getMastersCollege());
        }

        if (request.getMastersStartYear() != null) {
            education.setMastersStartYear(request.getMastersStartYear());
        }
    }

    @Override
    public EducationResponse getMyEducation() {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository
                .findByCandidate(candidate)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Education details not found."));

        return mapToResponse(education);
    }

    @Override
    public void deleteEducation() {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository.findByCandidate(candidate)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Education details not found."));

        educationRepository.delete(education);

        log.info("Education deleted for candidate {}", candidate.getId());
    }

}
