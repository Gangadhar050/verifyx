package com.verify_x.serviceImpl;

import com.verify_x.dto.HrOfferLetterRequestDTO;
import com.verify_x.dto.HrOfferLetterResponseDTO;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.HrOfferLetter;
import com.verify_x.enums.OfferLetterMailStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.HrOfferLetterRepository;
import com.verify_x.services.EmailService;
import com.verify_x.services.HrOfferLetterService;
import com.verify_x.util.OfferLetterPdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrOfferLetterServiceImpl implements HrOfferLetterService {

    private final CandidateRepository candidateRepository;
    private final HrOfferLetterRepository hrOfferLetterRepository;
    private final EmailService emailService;
    private final OfferLetterPdfGenerator pdfGenerator;

    // No class-level @Transactional: mail sending is slow and must not
    // hold a DB transaction open.
    @Override
    public HrOfferLetterResponseDTO sendOfferLetter(
            Long candidateId,
            HrOfferLetterRequestDTO request,
            String sentBy) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with id: " + candidateId));

        if (request.getJoiningDate().isBefore(request.getOfferDate())) {
            throw new BadRequestException("Joining date cannot be before offer date");
        }

        String referenceNumber = request.getReferenceNumber().trim();

        if (hrOfferLetterRepository.existsByReferenceNumber(referenceNumber)) {
            throw new BadRequestException(
                    "Reference number already used: " + referenceNumber);
        }

        String companyName = request.getCompanyName().trim();
        String designation = request.getDesignation().trim();

        // Generate the PDF first so a failure here saves nothing
        byte[] pdf = pdfGenerator.generate(new OfferLetterPdfGenerator.Data(
                candidate.getUsername(),
                candidate.getAddress(),
                companyName,
                designation,
                request.getOfferedCtc(),
                request.getOfferDate(),
                request.getJoiningDate(),
                referenceNumber,
                request.getEmploymentType()));

        HrOfferLetter offer = HrOfferLetter.builder()
                .candidate(candidate)
                .candidateName(candidate.getUsername())
                .candidateEmail(candidate.getEmail())
                .companyName(companyName)
                .designation(designation)
                .offeredCtc(request.getOfferedCtc())
                .offerDate(request.getOfferDate())
                .joiningDate(request.getJoiningDate())
                .referenceNumber(referenceNumber)
                .employmentType(request.getEmploymentType())
                .sentBy(sentBy)
                .mailStatus(OfferLetterMailStatus.FAILED)
                .build();

        HrOfferLetter saved = hrOfferLetterRepository.save(offer);

        emailService.sendOfferLetterEmail(
                saved.getCandidateEmail(),
                saved.getCandidateName(),
                saved.getCompanyName(),
                saved.getDesignation(),
                saved.getReferenceNumber(),
                pdf);

        saved.setMailStatus(OfferLetterMailStatus.SENT);
        saved = hrOfferLetterRepository.save(saved);

        return toResponse(saved);
    }

    @Override
    public List<HrOfferLetterResponseDTO> getSentOfferLetters(Long candidateId) {

        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException(
                    "Candidate not found with id: " + candidateId);
        }

        return hrOfferLetterRepository
                .findByCandidateIdOrderByCreatedAtDesc(candidateId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HrOfferLetterResponseDTO toResponse(HrOfferLetter o) {
        return HrOfferLetterResponseDTO.builder()
                .id(o.getId())
                .candidateId(o.getCandidate().getId())
                .candidateName(o.getCandidateName())
                .candidateEmail(o.getCandidateEmail())
                .companyName(o.getCompanyName())
                .designation(o.getDesignation())
                .offeredCtc(o.getOfferedCtc())
                .offerDate(o.getOfferDate())
                .joiningDate(o.getJoiningDate())
                .referenceNumber(o.getReferenceNumber())
                .employmentType(o.getEmploymentType())
                .mailStatus(o.getMailStatus())
                .sentBy(o.getSentBy())
                .createdAt(o.getCreatedAt())
                .build();
    }
}