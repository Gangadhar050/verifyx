package com.verify_x.serviceImpl;

import com.verify_x.services.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import com.verify_x.enums.EmploymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
//    @Value("${spring.mail.username}")
//    private String fromEmail;
@Value("${app.calendar.timezone:Asia/Kolkata}")
private String calendarTimeZone;
    @Value("${app.mail.fail-open:false}")
    private boolean failOpen;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.application-url:http://localhost:5173/candidate}")
    private String applicationUrl;

    @Value("${app.frontend.interview-url:http://localhost:5173/candidate/interview}")
    private String interviewUrl;

    @Value("${app.hr.email:admin@verifyx.com}")
    private String hrEmail;

    @PostConstruct
    public void checkMail() {
        System.out.println("================================");
        System.out.println("MAIL USER : " + fromEmail);
        System.out.println("================================");
    }

    @Override
    public void sendApplicationApprovedEmail(
            String to,
            String candidateName,
            String remarks) {

        String subject =
                "VerifyX | Application Approved";

        String body = """
            <html>
            <body style="font-family: Arial, sans-serif;">

            <h2>Congratulations %s!</h2>

            <p>
                Your application has been
                <b>APPROVED</b>.
            </p>

            <p>
                <b>HR Remarks:</b>
            </p>

            <p>%s</p>

            <hr>

            <h3>Interview Scheduling</h3>

            <p>
                Your application has been approved.
                Please select your preferred interview
                date and time from the available slots.
            </p>

            <p>
                <a href="%s"
                   target="_blank"
                   style="
                   display:inline-block;
                   padding:12px 24px;
                   background-color:#2563eb;
                   color:white;
                   text-decoration:none;
                   border-radius:6px;
                   font-weight:bold;">
                    Select Interview Slot
                </a>
            </p>

            <p>
                Once you select a slot, it will be
                reserved for you and will no longer
                be available to other candidates.
            </p>

            <br>

            <p>Regards,</p>

            <b>VerifyX HR Team</b>

            </body>
            </html>
            """
                .formatted(
                        candidateName,
                        remarks != null
                                ? remarks
                                : "No remarks provided.",
                        interviewUrl
                );

        sendHtmlMail(
                to,
                subject,
                body
        );
    }

    @Override
    public void sendApplicationRejectedEmail(
            String to,
            String candidateName,
            String remarks) {

        String subject = "VerifyX | Application Rejected";

        String body = """
                <html>
                <body>

                <h2>Hello %s,</h2>

                <p>Unfortunately your application has been <b>REJECTED</b>.</p>

                <p><b>Reason:</b></p>

                <p>%s</p>

                <p><a href="%s" target="_blank">Open VerifyX Application</a></p>

                <br>

                <p>Regards,</p>

                <b>VerifyX HR Team</b>

                </body>
                </html>
                """
                .formatted(candidateName, remarks, applicationUrl);

        sendHtmlMail(to, subject, body);
    }

    @Override
    public void sendReUploadRequestEmail(
            String to,
            String candidateName,
            String remarks) {

        String subject = "VerifyX | Document Re-upload Required";

        String body = """
                <html>
                <body>

                <h2>Hello %s,</h2>

                <p>Your application requires document re-upload.</p>

                <p><b>HR Remarks:</b></p>

                <p>%s</p>

                <br>

                <p>Please login to VerifyX and upload the requested documents.</p>

                <p><a href="%s" target="_blank">Open VerifyX Application</a></p>

                <br>

                <b>VerifyX HR Team</b>

                </body>
                </html>
                """
                .formatted(candidateName, remarks, applicationUrl);

        sendHtmlMail(to, subject, body);
    }

    /**
     * Common Email Sender
     */
    private void sendHtmlMail(
            String to,
            String subject,
            String body) {

        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("[VerifyX LOCAL MAIL] To: " + to + " | Subject: " + subject);
            return;
        }

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

        } catch (MessagingException | MailException ex) {
            if (failOpen) {
                System.err.println("[VerifyX MAIL WARNING] Email delivery failed; local flow will continue: " + ex.getMessage());
                return;
            }
            throw new RuntimeException("Unable to send email.", ex);
        }
    }

    @Override
    public void sendOtp(String email, String otp) {

        if (failOpen) {
            System.out.println("[VerifyX LOCAL OTP BACKUP] " + email + " -> " + otp);
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("[VerifyX LOCAL OTP] " + email + " -> " + otp);
            return;
        }

        String subject = "VerifyX | OTP Verification";

        String body = """
                <html>
                <body>

                <h2>Hello,</h2>

                <p>Your OTP for VerifyX is:</p>

                <h3>%s</h3>

                <br>

                <p>Please use this OTP to complete your verification process.</p>

                <br>

                <b>VerifyX Team</b>

                </body>
                </html>
                """
                .formatted(otp);

        sendHtmlMail(email, subject, body);
    }

    @Override
    public void sendInterviewSlotsEmail(
            String email,
            String candidateName,
            List<LocalDateTime> slots) {

        String subject = "VerifyX | Interview Slots";

        StringBuilder slotsHtml = new StringBuilder();

        for (LocalDateTime slot : slots) {
            slotsHtml.append("<li>")
                    .append(slot)
                    .append("</li>");
        }

        String body = """
            <html>
            <body>

            <h2>Hello %s,</h2>

            <p>Your application has been approved.</p>

            <p>Your interview slot options are:</p>

            <ul>
                %s
            </ul>

            <p>Please select a suitable slot in VerifyX and confirm your availability.</p>

            <p>A calendar file is attached. Open it using Google Calendar,
            Outlook, or Apple Calendar.</p>

            <br>

            <b>VerifyX HR Team</b>

            </body>
            </html>
            """
                .formatted(candidateName, slotsHtml.toString());

        sendHtmlMailWithCalendar(
                email,
                subject,
                body,
                candidateName,
                slots
        );
    }
    private void sendHtmlMailWithCalendar(
            String to,
            String subject,
            String body,
            String candidateName,
            List<LocalDateTime> slots) {

        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println(
                    "[VerifyX LOCAL MAIL] To: " + to
                            + " | Subject: " + subject
                            + " | Calendar slots: " + slots.size()
            );
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            helper.addAttachment(
                    "VerifyX-interview-options.ics",
                    new ByteArrayResource(
                            buildInterviewCalendar(candidateName, slots)
                    ),
                    "text/calendar; charset=UTF-8; method=PUBLISH"
            );

            mailSender.send(message);

        } catch (MessagingException | MailException ex) {

            if (failOpen) {
                System.err.println(
                        "[VerifyX MAIL WARNING] Calendar email delivery failed: "
                                + ex.getMessage()
                );
                return;
            }

            throw new RuntimeException(
                    "Unable to send calendar email.",
                    ex
            );
        }
    }

    private byte[] buildInterviewCalendar(
            String candidateName,
            List<LocalDateTime> slots) {

        ZoneId zone = ZoneId.of(calendarTimeZone);

        String now = DateTimeFormatter
                .ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .format(ZonedDateTime.now(ZoneId.of("UTC")));

        StringBuilder calendar = new StringBuilder();

        calendar.append("BEGIN:VCALENDAR\r\n");
        calendar.append("VERSION:2.0\r\n");
        calendar.append("PRODID:-//VerifyX//Interview Slots//EN\r\n");
        calendar.append("CALSCALE:GREGORIAN\r\n");
        calendar.append("METHOD:PUBLISH\r\n");

        for (LocalDateTime slot : slots) {

            ZonedDateTime start = slot.atZone(zone);
            ZonedDateTime end = start.plusHours(1);

            calendar.append("BEGIN:VEVENT\r\n");
            calendar.append("UID:")
                    .append(UUID.randomUUID())
                    .append("@verifyx\r\n");

            calendar.append("DTSTAMP:")
                    .append(now)
                    .append("\r\n");

            calendar.append("DTSTART:")
                    .append(toUtcCalendarTime(start))
                    .append("\r\n");

            calendar.append("DTEND:")
                    .append(toUtcCalendarTime(end))
                    .append("\r\n");

            calendar.append("SUMMARY:")
                    .append(escapeIcs("VerifyX interview option"))
                    .append("\r\n");

            calendar.append("DESCRIPTION:")
                    .append(escapeIcs(
                            "Proposed interview option for "
                                    + candidateName
                                    + ". Please select this slot in VerifyX to confirm."
                    ))
                    .append("\r\n");

            calendar.append("STATUS:TENTATIVE\r\n");
            calendar.append("END:VEVENT\r\n");
        }

        calendar.append("END:VCALENDAR\r\n");

        return calendar.toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private String toUtcCalendarTime(ZonedDateTime dateTime) {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .format(
                        dateTime.withZoneSameInstant(
                                ZoneId.of("UTC")
                        )
                );
    }

    private String escapeIcs(String value) {
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }

    @Override
    public void sendInterviewSlotSelectedEmailToHr(
            String candidateName,
            String candidateEmail,
            LocalDateTime selectedSlot) {

        String subject = "VerifyX | Interview Slot Selected";

        String body = """
                <html>
                <body>

                <h2>Interview slot selected</h2>

                <p>Candidate <b>%s</b> (%s) has selected the following interview slot:</p>

                <p><b>%s</b></p>

                <br>

                <p>Please prepare for the interview at the agreed time.</p>

                <br>

                <b>VerifyX System</b>

                </body>
                </html>
                """
                .formatted(candidateName, candidateEmail, selectedSlot);

        sendHtmlMail(hrEmail, subject, body);
    }
    @Override
    public void sendOfferLetterEmail(
            String to,
            String candidateName,
            String companyName,
            String designation,
            String referenceNumber,
            byte[] offerLetterPdf) {

        String subject = "Offer Letter | " + companyName
                + " | Ref: " + referenceNumber;

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                <h3>Dear %s,</h3>
                <p>
                    Congratulations! Please find attached your offer letter for the
                    position of <b>%s</b> at <b>%s</b>.
                </p>
                <p>Please sign and return a copy to confirm your acceptance.</p>
                <br>
                <p>Regards,</p>
                <b>%s HR Team</b>
                </body>
                </html>
                """.formatted(
                esc(candidateName),
                esc(designation),
                esc(companyName),
                esc(companyName));

        String fileName = "Offer_Letter_"
                + referenceNumber.replaceAll("[^A-Za-z0-9._-]", "_") + ".pdf";

        sendHtmlMailWithPdf(to, subject, body, fileName, offerLetterPdf);
    }

    private void sendHtmlMailWithPdf(
            String to,
            String subject,
            String body,
            String fileName,
            byte[] pdf) {

        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("[VerifyX LOCAL MAIL] To: " + to
                    + " | Subject: " + subject + " | Attachment: " + fileName);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment(fileName, new ByteArrayResource(pdf), "application/pdf");

            mailSender.send(message);

        } catch (MessagingException | MailException ex) {
            if (failOpen) {
                System.err.println("[VerifyX MAIL WARNING] Offer letter email failed: "
                        + ex.getMessage());
                return;
            }
            throw new RuntimeException("Unable to send offer letter email.", ex);
        }
    }

    // Prevent HTML injection from HR-typed values
    private String esc(String v) {
        return v == null ? "" : v
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

}