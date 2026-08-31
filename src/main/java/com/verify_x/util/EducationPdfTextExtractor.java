package com.verify_x.util;

import com.verify_x.enums.EducationDocumentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class EducationPdfTextExtractor {

    public Map<String, String> extract(MultipartFile file, EducationDocumentType type) {
        Map<String, String> result = new LinkedHashMap<>();
        if (file == null || file.isEmpty()) return result;

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

        if (!"application/pdf".equals(contentType) && !name.endsWith(".pdf")) {
            return result;
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            if (text == null || text.isBlank()) return result;

            text = text.replace('\u0000', ' ')
                    .replace('\r', '\n')
                    .replaceAll("[\\t ]+", " ")
                    .replaceAll("\\n{2,}", "\n");

            switch (type) {
                case TENTH_MARKS_CARD -> parseTenth(text, result);
                case TWELFTH_MARKS_CARD -> parseTwelfth(text, result);
                case DEGREE_CERTIFICATE -> parseDegree(text, result);
                case MASTERS_MARKS_CARD, MASTERS_DEGREE_CERTIFICATE -> parseMasters(text, result);
            }

            log.info("Local PDF extraction for {} found {} fields", type, result.size());
            return result;
        } catch (Exception e) {
            log.warn("Local PDF text extraction failed for {}: {}", file.getOriginalFilename(), e.getMessage());
            return result;
        }
    }

    private void parseTenth(String text, Map<String, String> out) {
        put(out, "tenthSchoolName", first(text,
                "(?i)(?:school|institution)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{4,100})"));
        put(out, "tenthBoard", first(text,
                "(?i)(CBSE|ICSE|CISCE|KSEAB|KSEEB|KARNATAKA SECONDARY EDUCATION EXAMINATION BOARD|STATE BOARD[^\\n]*)"));
        put(out, "tenthSchoolLocation", first(text,
                "(?i)(?:school\\s*location|location|place)\\s*[:\\-]?\\s*([^\\n]{2,80})"));
        put(out, "tenthRollNumber", first(text,
                "(?i)(?:roll\\s*(?:no|number)|registration\\s*(?:no|number)|reg\\.?\\s*no)\\s*[:\\-]?\\s*([A-Z0-9/\\-]{3,30})"));
        put(out, "tenthPassingYear", year(text));
        put(out, "tenthPercentage", percentage(text));
    }

    private void parseTwelfth(String text, Map<String, String> out) {
        put(out, "twelfthInstitutionName", first(text,
                "(?i)(?:college|institution|school)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{4,100})"));
        put(out, "twelfthBoardUniversity", first(text,
                "(?i)(?:board|university)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{3,100})"));
        put(out, "twelfthLocation", first(text,
                "(?i)(?:college\\s*location|institution\\s*location|location|place)\\s*[:\\-]?\\s*([^\\n]{2,80})"));
        put(out, "twelfthRegistrationNumber", first(text,
                "(?i)(?:registration\\s*(?:no|number)|reg\\.?\\s*no|roll\\s*(?:no|number))\\s*[:\\-]?\\s*([A-Z0-9/\\-]{3,30})"));
        put(out, "twelfthPassingYear", year(text));
        put(out, "twelfthPercentage", percentage(text));
    }

    private void parseDegree(String text, Map<String, String> out) {
        put(out, "degreeName", first(text,
                "(?i)(?:degree|course|programme|program)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{2,80})",
                "(?i)\\b(B\\.?\\s*E\\.?|B\\.?\\s*TECH|BSC|BCA|BBA|BCOM|B\\.COM|BACHELOR OF [A-Z &]+)\\b"));
        put(out, "specialization", first(text,
                "(?i)(?:speciali[sz]ation|branch|discipline)\\s*[:\\-]?\\s*([^\\n]{2,80})"));
        put(out, "collegeName", first(text,
                "(?i)(?:college|institution)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{4,120})"));
        put(out, "universityName", first(text,
                "(?i)(?:university)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{4,120})"));
        put(out, "degreeLocation", first(text,
                "(?i)(?:college\\s*location|location|place)\\s*[:\\-]?\\s*([^\\n]{2,80})"));
        put(out, "usnNumber", first(text,
                "(?i)(?:USN|university\\s*seat\\s*number|registration\\s*(?:no|number))\\s*[:\\-]?\\s*([A-Z0-9/\\-]{3,30})"));

        Matcher years = Pattern.compile("\\b((?:19|20)\\d{2})\\b").matcher(text);
        String firstYear = "";
        String lastYear = "";
        while (years.find()) {
            if (firstYear.isBlank()) firstYear = years.group(1);
            lastYear = years.group(1);
        }
        put(out, "degreeStartYear", first(text,
                "(?i)(?:start|admission|from)\\s*(?:year)?\\s*[:\\-]?\\s*((?:19|20)\\d{2})",
                firstYear.isBlank() ? "(?!)" : Pattern.quote(firstYear)));
        put(out, "degreeEndYear", first(text,
                "(?i)(?:end|passing|graduation|completed|to)\\s*(?:year)?\\s*[:\\-]?\\s*((?:19|20)\\d{2})",
                lastYear.isBlank() ? "(?!)" : Pattern.quote(lastYear)));
        put(out, "degreePercentage", percentage(text));
    }

    private void parseMasters(String text, Map<String, String> out) {
        put(out, "mastersDegree", first(text,
                "(?i)(?:degree|course|programme|program)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{2,80})",
                "(?i)\\b(M\\.?\\s*TECH|MSC|MCA|MBA|MCOM|M\\.COM|MASTER OF [A-Z &]+)\\b"));
        put(out, "mastersSpecialization", first(text,
                "(?i)(?:speciali[sz]ation|branch|discipline)\\s*[:\\-]?\\s*([^\\n]{2,80})"));
        put(out, "mastersCollege", first(text,
                "(?i)(?:college|institution)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{4,120})"));
        put(out, "mastersUniversity", first(text,
                "(?i)(?:university)\\s*(?:name)?\\s*[:\\-]?\\s*([^\\n]{4,120})"));
        put(out, "mastersLocation", first(text,
                "(?i)(?:college\\s*location|location|place)\\s*[:\\-]?\\s*([^\\n]{2,80})"));
        put(out, "mastersRegistrationNumber", first(text,
                "(?i)(?:registration\\s*(?:no|number)|reg\\.?\\s*no|USN)\\s*[:\\-]?\\s*([A-Z0-9/\\-]{3,30})"));
        put(out, "mastersStartYear", first(text,
                "(?i)(?:start|admission|from)\\s*(?:year)?\\s*[:\\-]?\\s*((?:19|20)\\d{2})"));
        put(out, "mastersEndYear", first(text,
                "(?i)(?:end|passing|graduation|completed|to)\\s*(?:year)?\\s*[:\\-]?\\s*((?:19|20)\\d{2})"));
        put(out, "mastersPercentage", percentage(text));
    }

    private String year(String text) {
        return first(text,
                "(?i)(?:year\\s*of\\s*passing|passing\\s*year|month\\s*&\\s*year\\s*of\\s*exam|exam\\s*year)\\s*[:\\-]?\\s*(?:[A-Za-z]+\\s*)?((?:19|20)\\d{2})",
                "\\b((?:19|20)\\d{2})\\b");
    }

    private String percentage(String text) {
        return first(text,
                "(?i)(?:percentage|percent|aggregate|overall)\\s*[:\\-]?\\s*(\\d{1,3}(?:\\.\\d{1,2})?)\\s*%?",
                "(?i)(\\d{1,3}(?:\\.\\d{1,2})?)\\s*%");
    }

    private String first(String text, String... patterns) {
        for (String regex : patterns) {
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) {
                String value = m.groupCount() >= 1 ? m.group(1) : m.group();
                if (value != null) {
                    value = value.trim().replaceAll("\\s{2,}", " ");
                    if (!value.isBlank()) return value;
                }
            }
        }
        return "";
    }

    private void put(Map<String, String> out, String key, String value) {
        if (value != null && !value.isBlank()) out.put(key, value.trim());
    }
}
