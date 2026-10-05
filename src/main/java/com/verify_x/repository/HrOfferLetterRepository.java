package com.verify_x.repository;

import com.verify_x.entity.HrOfferLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrOfferLetterRepository
        extends JpaRepository<HrOfferLetter, Long> {

    boolean existsByReferenceNumber(String referenceNumber);

    List<HrOfferLetter> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}