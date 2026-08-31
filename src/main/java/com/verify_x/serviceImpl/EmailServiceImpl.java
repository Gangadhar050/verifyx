package com.verify_x.serviceImpl;

import com.verify_x.services.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
//    @Value("${spring.mail.username}")
//    private String fromEmail;

    @Value("${app.mail.fail-open:false}")
    private boolean failOpen;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.application-url:http://localhost:5173/candidate}")
    private String applicationUrl;

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

        String subject = "VerifyX | Application Approved";

        String body = """
                <html>
                <body>

                <h2>Congratulations %s!</h2>

                <p>Your application has been <b>APPROVED</b>.</p>

                <p><b>HR Remarks:</b></p>

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

}