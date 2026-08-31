//package com.verify_x.repository;
//
//import com.verify_x.entity.Candidate;
//import com.verify_x.entity.CandidateContactDetail;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface CandidateContactDetailsRepository1
//        extends JpaRepository<CandidateContactDetail, Long> {
//
//    /**
//     * Find contact details using candidate entity.
//     */
//    Optional<CandidateContactDetail> findByCandidate(
//            Candidate candidate
//    );
//
//    /**
//     * Find contact details using candidate ID.
//     */
//    Optional<CandidateContactDetail> findByCandidateId(
//            Long candidateId
//    );
//
//    /**
//     * Check whether candidate already has contact details.
//     */
//    boolean existsByCandidate(
//            Candidate candidate
//    );
//
//    /**
//     * Check using candidate ID.
//     */
//    boolean existsByCandidateId(
//            Long candidateId
//    );
//}