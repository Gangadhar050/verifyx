package com.verify_x.entity;

import com.verify_x.enums.EmploymentType;
import com.verify_x.enums.OfferLetterMailStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Offer letter issued BY HR TO a candidate.
 * (Different from OfferLetter, which is a previous-employer
 * document uploaded BY the candidate.)
 */
@Entity
@Table(
        name = "hr_offer_letters",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_hr_offer_reference_number",
                columnNames = "reference_number"),
        indexes = @Index(
                name = "idx_hr_offer_candidate_id",
                columnList = "candidate_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrOfferLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    // Snapshot of what was actually sent (auto-fetched from Candidate)
    @Column(nullable = false, length = 100)
    private String candidateName;

    @Column(nullable = false)
    private String candidateEmail;

    // HR-entered fields
    @Column(nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 150)
    private String designation;

    // CTC in LPA, e.g. 7.50
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredCtc;

    @Column(nullable = false)
    private LocalDate offerDate;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Column(name = "reference_number", nullable = false, length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    // Audit
    @Column(length = 255)
    private String sentBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OfferLetterMailStatus mailStatus;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}