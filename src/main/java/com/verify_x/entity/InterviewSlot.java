package com.verify_x.entity;

import com.verify_x.enums.InterviewSlotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "interview_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_interview_slot_datetime",
                        columnNames = "slot_date_time"
                )
        },
        indexes = {
                @Index(
                        name = "idx_interview_slot_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_interview_slot_datetime",
                        columnList = "slot_date_time"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Slot is GLOBAL.
     *
     * It does not belong to a candidate until
     * the candidate selects it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Column(
            name = "slot_date_time",
            nullable = false
    )
    private LocalDateTime slotDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InterviewSlotStatus status =
            InterviewSlotStatus.AVAILABLE;

    private LocalDateTime selectedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}