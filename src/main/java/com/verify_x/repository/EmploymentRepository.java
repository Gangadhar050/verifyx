package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.Employment;
import com.verify_x.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {

    /*
     * Find by Candidate
     */

    Optional<Employment> findByCandidate(Candidate candidate);

    Optional<Employment> findByCandidateId(Long candidateId);

    /*
     * Exists
     */

    boolean existsByCandidate(Candidate candidate);

    /*
     * Delete
     */

    void deleteByCandidate(Candidate candidate);

    /*
     * Search
     */

    List<Employment> findByCurrentCompanyContainingIgnoreCase(String company);


    List<Employment> findByEmploymentStatus(EmploymentStatus employmentStatus);

    List<Employment> findByPreviousCompanyNameContainingIgnoreCase(String keyword);

    List<Employment> findByCurrentDesignationContainingIgnoreCase(String keyword);

    List<Employment> findByPreviousDesignationContainingIgnoreCase(String keyword);
}