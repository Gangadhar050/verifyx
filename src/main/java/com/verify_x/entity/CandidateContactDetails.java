package com.verify_x.entity;

import jakarta.persistence.*;
import lombok.*;

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


    // =========================================================
    // PERMANENT ADDRESS
    // =========================================================

    @Column(
            name = "permanent_address_line1",
            nullable = false,
            length = 500
    )
    private String permanentAddressLine1;

    @Column(
            name = "permanent_address_line2",
            length = 500
    )
    private String permanentAddressLine2;

    @Column(
            name = "permanent_area_locality",
            length = 255
    )
    private String permanentAreaLocality;

    @Column(
            name = "permanent_pincode",
            nullable = false,
            length = 10
    )
    private String permanentPincode;

    @Column(
            name = "permanent_city",
            nullable = false,
            length = 100
    )
    private String permanentCity;

    @Column(
            name = "permanent_district",
            length = 100
    )
    private String permanentDistrict;

    @Column(
            name = "permanent_state",
            nullable = false,
            length = 100
    )
    private String permanentState;

    @Column(
            name = "permanent_country",
            nullable = false,
            length = 100
    )
    private String permanentCountry;

    /**
     * Coordinates returned by Google Maps when
     * candidate selects a permanent address.
     *
     * They can be null when candidate enters
     * address manually.
     */
//    @Column(name = "permanent_latitude")
//    private Double permanentLatitude;
//
//    @Column(name = "permanent_longitude")
//    private Double permanentLongitude;


    // =========================================================
    // CURRENT ADDRESS
    // =========================================================

    /**
     * Address generated from GPS coordinates
     * using reverse geocoding.
     */
    @Column(
            name = "current_address",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String currentAddress;

    @Column(
            name = "current_pincode",
            length = 10
    )
    private String currentPincode;

    @Column(
            name = "current_city",
            length = 100
    )
    private String currentCity;

    @Column(
            name = "current_district",
            length = 100
    )
    private String currentDistrict;

    @Column(
            name = "current_state",
            length = 100
    )
    private String currentState;

    @Column(
            name = "current_country",
            length = 100
    )
    private String currentCountry;

    /**
     * GPS coordinates captured from browser/device.
     */
    @Column(
            name = "current_latitude",
            nullable = false
    )
    private Double currentLatitude;

    @Column(
            name = "current_longitude",
            nullable = false
    )
    private Double currentLongitude;

    /**
     * Current-address live photo.
     *
     * Maximum allowed by service: 5 MB.
     */
    @Lob
    @Column(
            name = "current_address_photo",
            nullable = false,
            columnDefinition = "LONGBLOB"
    )
    private byte[] currentAddressPhoto;

    @Column(
            name = "current_address_photo_name",
            nullable = false,
            length = 255
    )
    private String currentAddressPhotoName;

    @Column(
            name = "current_address_photo_content_type",
            nullable = false,
            length = 100
    )
    private String currentAddressPhotoContentType;


    // =========================================================
    // CONTACT NUMBERS
    // =========================================================

    @Column(
            name = "emergency_contact_number",
            nullable = false,
            length = 15
    )
    private String emergencyContactNumber;

    /**
     * Referral contact is optional.
     */
    @Column(
            name = "referral_contact_number",
            length = 15
    )
    private String referralContactNumber;


    // =========================================================
    // AUDIT
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    // =========================================================
    // JPA CALLBACKS
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}