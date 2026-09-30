package com.verify_x.services;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailService {

    void sendApplicationApprovedEmail(
            String to,
            String candidateName,
            String remarks
    );

    void sendApplicationRejectedEmail(
            String to,
            String candidateName,
            String remarks
    );

    void sendReUploadRequestEmail(
            String to,
            String candidateName,
            String remarks
    );
    void sendOtp(String email, String otp);

    void sendInterviewSlotsEmail(
            String email,
            String candidateName,
            List<LocalDateTime> slots);
}
