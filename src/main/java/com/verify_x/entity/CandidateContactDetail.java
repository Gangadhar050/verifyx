//package com.verify_x.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(
//        name = "candidate_contact_details",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_candidate_contact_details_candidate",
//                        columnNames = "candidate_id"
//                )
//        },
//        indexes = {
//                @Index(
//                        name = "idx_candidate_contact_details_candidate",
//                        columnList = "candidate_id"
//                )
//        }
//)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CandidateContactDetail {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    /**
//     * One contact/address record belongs to exactly one candidate.
//     */
//    @OneToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(
//            name = "candidate_id",
//            nullable = false,
//            unique = true,
//            foreignKey = @ForeignKey(
//                    name = "fk_candidate_contact_details_candidate"
//            )
//    )
//    private Candidate candidate;
//
//    /**
//     * Permanent address entered manually by candidate.
//     */
//    @Column(
//            name = "permanent_address",
//            nullable = false,
//            length = 1000
//    )
//    private String permanentAddress;
//
//    /**
//     * Current/residential address entered manually by candidate.
//     */
//    @Column(
//            name = "residential_address",
//            nullable = false,
//            length = 1000
//    )
//    private String residentialAddress;
//
//    /**
//     * Mandatory emergency contact.
//     */
//    @Column(
//            name = "emergency_contact_number",
//            nullable = false,
//            length = 10
//    )
//    private String emergencyContactNumber;
//
//    /**
//     * Optional referral contact.
//     */
//    @Column(
//            name = "referral_contact_number",
//            length = 10
//    )
//    private String referralContactNumber;
//
//    @Column(
//            name = "created_at",
//            nullable = false,
//            updatable = false
//    )
//    private LocalDateTime createdAt;
//
//    @Column(
//            name = "updated_at",
//            nullable = false
//    )
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        createdAt = now;
//        updatedAt = now;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//
//        updatedAt = LocalDateTime.now();
//    }
//}