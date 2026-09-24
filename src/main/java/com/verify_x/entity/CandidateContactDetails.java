package com.verify_x.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "candidate_contact_details",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_contact_candidate",
                        columnNames = "candidate_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_candidate_contact_candidate",
                        columnList = "candidate_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateContactDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // CANDIDATE
    // =========================================================

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_contact_details_candidate"
            )
    )
    private Candidate candidate;

    // Permanent
    private String permanentAddressLine1;
    private String permanentAddressLine2;
    private String permanentAreaLocality;
    private String permanentPincode;
    private String permanentCity;
    private String permanentDistrict;
    private String permanentState;
    private String permanentCountry;
    private String emergencyContactNumber;
    private String referralContactNumber;

    // Current
    private String currentAddress;
    private String currentAddressLine1;
    private String currentAddressLine2;
    private String currentAreaLocality;
    private String currentPincode;
    private String currentCity;
    private String currentDistrict;
    private String currentState;
    private String currentCountry;
//    private Double currentLatitude;
//    private Double currentLongitude;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}