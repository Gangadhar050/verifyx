package com.verify_x.entity;

import com.verify_x.enums.EmploymentStatus;
import com.verify_x.enums.NoticePeriodStatus;
import com.verify_x.enums.OfferLetterStatus;
import com.verify_x.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employment_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Candidate Mapping
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    /*
     * Current Employment
     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus employmentStatus;

    private String currentCompany;

    private String currentDesignation;

    private Double currentCTC;

    private LocalDate workingFrom;

    /*
     * Notice Period
     */

    @Enumerated(EnumType.STRING)
    private NoticePeriodStatus noticePeriodStatus;

    private Integer officialNoticePeriod;

    private LocalDate lastWorkingDate;

    /*
     * Previous Employment
     */

    private String previousCompanyName;

    private String previousDesignation;

    private LocalDate previousWorkingFrom;

    private LocalDate previousWorkingTo;

    private Double previousCTC;

    private Double expectedCTC;

    private Double totalExperience;

    /*
     * UAN
     */

    @Column(length = 12)
    private String uanNumber;

    @Builder.Default
    private Boolean uanVerified = false;

    private String uanVerifiedBy;

    private LocalDateTime uanVerifiedAt;

    @Enumerated(EnumType.STRING)
    private  OfferLetterStatus offerLetterStatus;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus uanVerificationStatus =
            VerificationStatus.PENDING;

    /*
     * Audit
     */

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}