package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.Interview;
import com.verify_x.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository
        extends JpaRepository<Interview, Long> {

    Optional<Interview> findByCandidate(Candidate candidate);

    Optional<Interview> findByCandidateId(Long candidateId);

    List<Interview> findByInterviewStatus(InterviewStatus interviewStatus);
}