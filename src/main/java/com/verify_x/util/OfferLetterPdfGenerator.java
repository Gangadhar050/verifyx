package com.verify_x.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.verify_x.enums.EmploymentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class OfferLetterPdfGenerator {

    public record Data(
            String candidateName,
            String candidateAddress,
            String companyName,
            String designation,
            BigDecimal ctcLpa,
            LocalDate offerDate,
            LocalDate joiningDate,
            String referenceNumber,
            EmploymentType employmentType) {
    }

    // ---- Fixed content, configurable in application.properties ----
    @Value("${app.offer.department:}")
    private String department;
    @Value("${app.offer.work-location:}")
    private String workLocation;
    @Value("${app.offer.reporting-to:}")
    private String reportingTo;
    @Value("${app.offer.working-hours:09:30 AM to 06:30 PM}")
    private String workingHours;
    @Value("${app.offer.working-days:Monday to Friday}")
    private String workingDays;
    @Value("${app.offer.acceptance-days:4}")
    private int acceptanceDays;
    @Value("${app.offer.signatory-name:HR Manager}")
    private String signatoryName;
    @Value("${app.offer.signatory-title:HR Manager}")
    private String signatoryTitle;
    @Value("${app.offer.hr-email:}")
    private String hrEmail;
    @Value("${app.offer.company-address:}")
    private String companyAddress;

    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10.5f);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f);
    private static final Font HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
    private static final Font TITLE = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 20f, new BaseColor(37, 99, 235));

    public byte[] generate(Data d) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 70, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new RefHeader(d.referenceNumber()));
            doc.open();

            // Logo (optional): put file at src/main/resources/static/company-logo.png
            if (!addImage(doc, "static/company-logo.png", 170, 60, Element.ALIGN_LEFT)) {
                Paragraph name = new Paragraph(d.companyName(), TITLE);
                name.setSpacingAfter(20);
                doc.add(name);
            } else {
                doc.add(spacer(14));
            }

            // Date
            Phrase dateLine = new Phrase();
            dateLine.add(new Chunk("Date: ", BOLD));
            appendDate(dateLine, d.offerDate(), BOLD);
            doc.add(para(dateLine, 8));

            // To
            doc.add(para(new Phrase("To,", BOLD), 4));
            doc.add(para(new Phrase(d.candidateName(), BOLD), 2));
            if (notBlank(d.candidateAddress())) {
                doc.add(para(new Phrase(d.candidateAddress(), NORMAL), 12));
            } else {
                doc.add(spacer(8));
            }

            // Greeting
            Phrase dear = new Phrase();
            dear.add(new Chunk("Dear ", NORMAL));
            dear.add(new Chunk(d.candidateName(), BOLD));
            dear.add(new Chunk(",", NORMAL));
            doc.add(para(dear, 8));

            Phrase intro = new Phrase();
            intro.add(new Chunk("We are pleased to offer you the position of ", NORMAL));
            intro.add(new Chunk(d.designation(), BOLD));
            intro.add(new Chunk(" at ", NORMAL));
            intro.add(new Chunk(d.companyName(), BOLD));
            intro.add(new Chunk(". Your skills and background have impressed us, "
                    + "and we are excited to have you join our team.", NORMAL));
            doc.add(para(intro, 10));

            // Position details
            doc.add(heading("Position Details"));
            com.itextpdf.text.List details = bullets();
            details.add(labelItem("Job Title", d.designation()));
            details.add(labelItem("Employment Type",
                    d.employmentType().name().replace('_', ' ')));
            if (notBlank(department)) details.add(labelItem("Department", department));
            Phrase joining = new Phrase();
            joining.add(new Chunk("Joining Date: ", BOLD));
            appendDate(joining, d.joiningDate(), NORMAL);
            details.add(new ListItem(joining));
            if (notBlank(workLocation)) details.add(labelItem("Work Location", workLocation));
            if (notBlank(reportingTo)) details.add(labelItem("Reporting To", reportingTo));
            doc.add(details);
            doc.add(separator());

            // Compensation
            doc.add(heading("Compensation:"));
            Phrase comp = new Phrase();
            comp.add(new Chunk("Your annual compensation will be ", NORMAL));
            comp.add(new Chunk(formatInr(d.ctcLpa()), BOLD));
            comp.add(new Chunk(" [" + d.ctcLpa().stripTrailingZeros().toPlainString()
                    + " LPA] per annum, payable according to company policies "
                    + "and applicable deductions.", NORMAL));
            doc.add(para(comp, 6));
            doc.add(separator());

            // Working hours
            doc.add(heading("Working Hours:"));
            doc.add(para(new Phrase("Your regular working hours will be from " + workingHours
                    + ", working days will be " + workingDays + ".", NORMAL), 6));
            doc.add(separator());

            // Responsibilities
            doc.add(heading("Responsibilities:"));
            doc.add(para(new Phrase("Your responsibilities will include:", NORMAL), 4));
            com.itextpdf.text.List resp = bullets();
            resp.add(new ListItem(new Phrase(
                    "Carrying out the duties of the " + d.designation() + " role.", NORMAL)));
            resp.add(new ListItem(new Phrase(
                    "Delivering quality work within agreed timelines.", NORMAL)));
            resp.add(new ListItem(new Phrase(
                    "Collaborating with team members on projects.", NORMAL)));
            resp.add(new ListItem(new Phrase(
                    "Learning and adopting new tools and technologies as required.", NORMAL)));
            doc.add(resp);
            doc.add(separator());

            // Employment terms
            doc.add(heading("Employment Terms:"));
            com.itextpdf.text.List terms = bullets();
            terms.add(new ListItem(new Phrase("This offer is contingent on successful completion "
                    + "of onboarding documentation and background verification.", NORMAL)));
            terms.add(new ListItem(new Phrase("You are required to follow all company policies, "
                    + "code of conduct, and confidentiality norms.", NORMAL)));
            terms.add(new ListItem(new Phrase("Work location, project allocation, and shift "
                    + "timings will be communicated based on business requirements.", NORMAL)));
            doc.add(terms);
            doc.add(separator());

            // Acceptance
            Phrase accept = new Phrase();
            accept.add(new Chunk("Please sign and return a copy of this letter by ", NORMAL));
            appendDate(accept, d.offerDate().plusDays(acceptanceDays), NORMAL);
            accept.add(new Chunk(" to confirm your acceptance.", NORMAL));
            doc.add(para(accept, 8));

            doc.add(para(new Phrase("We are excited to welcome you to " + d.companyName()
                    + " and look forward to working with you.", NORMAL), 12));

            // Signature block
            doc.add(para(new Phrase("Warm Regards,", NORMAL), 0));
            doc.add(para(new Phrase(signatoryName, BOLD), 0));
            doc.add(para(new Phrase(signatoryTitle + " \u2013 " + d.companyName(), BOLD), 0));
            if (notBlank(hrEmail)) {
                Phrase email = new Phrase();
                email.add(new Chunk("Email: ", BOLD));
                email.add(new Chunk(hrEmail, NORMAL));
                doc.add(para(email, 0));
            }
            if (notBlank(companyAddress)) {
                doc.add(para(new Phrase(companyAddress, SMALL), 8));
            }

            // Signature/stamp (optional): src/main/resources/static/hr-stamp.png
            addImage(doc, "static/hr-stamp.png", 90, 90, Element.ALIGN_LEFT);

            doc.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate offer letter PDF", e);
        }
    }

    // ---------------- helpers ----------------

    private static class RefHeader extends PdfPageEventHelper {
        private final String ref;

        RefHeader(String ref) {
            this.ref = ref;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase("Reference ID : " + ref, SMALL),
                    document.right(),
                    document.top() + 30,
                    0);
        }
    }

    private Paragraph para(Phrase phrase, float spacingAfter) {
        Paragraph p = new Paragraph(phrase);
        p.setSpacingAfter(spacingAfter);
        return p;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }

    private Paragraph heading(String text) {
        Paragraph p = new Paragraph(text, HEADING);
        p.setSpacingBefore(6);
        p.setSpacingAfter(6);
        return p;
    }

    private Paragraph separator() {
        Paragraph p = new Paragraph(new Chunk(
                new LineSeparator(0.8f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
        p.setSpacingBefore(4);
        p.setSpacingAfter(4);
        return p;
    }

    private com.itextpdf.text.List bullets() {
        com.itextpdf.text.List list = new com.itextpdf.text.List(false, 14f);
        list.setListSymbol(new Chunk("\u2022", NORMAL));
        list.setIndentationLeft(14f);
        return list;
    }

    private ListItem labelItem(String label, String value) {
        Phrase p = new Phrase();
        p.add(new Chunk(label + ": ", BOLD));
        p.add(new Chunk(value, NORMAL));
        return new ListItem(p);
    }

    // 20TH May 2026 (superscript ordinal, like the sample)
    private void appendDate(Phrase target, LocalDate date, Font font) {
        int day = date.getDayOfMonth();
        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "TH";
        } else {
            switch (day % 10) {
                case 1 -> suffix = "ST";
                case 2 -> suffix = "ND";
                case 3 -> suffix = "RD";
                default -> suffix = "TH";
            }
        }
        Font supFont = new Font(font);
        supFont.setSize(7f);
        Chunk sup = new Chunk(suffix, supFont);
        sup.setTextRise(4f);

        target.add(new Chunk(String.valueOf(day), font));
        target.add(sup);
        target.add(new Chunk(" " + date.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)), font));
    }

    // 3.00 LPA -> "3,00,000" (Indian grouping)
    private String formatInr(BigDecimal lpa) {
        BigDecimal rupees = lpa.multiply(BigDecimal.valueOf(100000))
                .setScale(0, RoundingMode.HALF_UP);
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("en-IN"));
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        return nf.format(rupees);
    }

    private boolean addImage(Document doc, String classpath,
                             float maxW, float maxH, int align) {
        try {
            ClassPathResource res = new ClassPathResource(classpath);
            if (!res.exists()) return false;
            try (InputStream in = res.getInputStream()) {
                Image img = Image.getInstance(in.readAllBytes());
                img.scaleToFit(maxW, maxH);
                img.setAlignment(align);
                doc.add(img);
                return true;
            }
        } catch (Exception ignored) {
            return false; // images are optional
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}