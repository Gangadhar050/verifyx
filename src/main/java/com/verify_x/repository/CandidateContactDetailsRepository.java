package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateContactDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateContactDetailsRepository
        extends JpaRepository<CandidateContactDetails, Long> {

    Optional<CandidateContactDetails> findByCandidate(
            Candidate candidate);

    Optional<CandidateContactDetails> findByCandidateId(
            Long candidateId);

    boolean existsByCandidate(
            Candidate candidate
    );
}