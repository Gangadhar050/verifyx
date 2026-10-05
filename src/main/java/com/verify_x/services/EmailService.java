package com.verify_x.services;

import com.verify_x.enums.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    void sendInterviewSlotSelectedEmailToHr(
            String candidateName,
            String candidateEmail,
            LocalDateTime selectedSlot);


       void sendOfferLetterEmail(
            String to,
            String candidateName,
            String companyName,
            String designation,
            String referenceNumber,
            byte[] offerLetterPdf);
}
