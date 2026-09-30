package com.verify_x.entity;

import com.verify_x.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_slot_id")
    private InterviewSlot selectedSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InterviewStatus interviewStatus =
            InterviewStatus.AWAITING_SLOT;

    private String interviewRemarks;

    private String confirmedBy;

    private LocalDateTime confirmedAt;

    private String result;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}