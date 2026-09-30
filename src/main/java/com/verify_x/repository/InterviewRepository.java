package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewRepository
        extends JpaRepository<Interview, Long> {

    Optional<Interview> findByCandidate(Candidate candidate);

    boolean existsByCandidate(Candidate candidate);
}