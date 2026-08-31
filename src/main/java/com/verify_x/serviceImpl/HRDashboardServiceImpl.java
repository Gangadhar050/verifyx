package com.verify_x.serviceImpl;

import com.verify_x.dto.HRCandidateDashboardDto;
import com.verify_x.dto.HRDashboardResponseDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.HRDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HRDashboardServiceImpl
        implements HRDashboardService {

    private final CandidateRepository candidateRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;

    @Override
    public HRDashboardResponseDto getDashboardReport() {

        long total =
                candidateRepository.count();

        long freshers =
                candidateRepository.findAll()
                        .stream()
                        .filter(candidate ->
                                candidate.getCandidateType()
                                        == CandidateType.FRESHER)
                        .count();

        long experienced =
                candidateRepository.findAll()
                        .stream()
                        .filter(candidate ->
                                candidate.getCandidateType()
                                        == CandidateType.EXPERIENCED)
                        .count();

        long pending =
                candidateRepository.countByApplicationStatus(
                        ApplicationStatus.PENDING_VERIFICATION
                );

        long approved =
                candidateRepository.countByApplicationStatus(
                        ApplicationStatus.APPROVED
                );

        long rejected =
                candidateRepository.countByApplicationStatus(
                        ApplicationStatus.RE_UPLOAD_REQUIRED
                );

        return new HRDashboardResponseDto(
                total,
                freshers,
                experienced,
                pending,
                approved,
                rejected
        );
    }

    @Override
    public List<HRCandidateDashboardDto> getCandidates() {

        return candidateRepository.findAll()
                .stream()
                .map(candidate -> {
                    String appliedRole =
                            candidate.getAppliedRole() != null
                                    ? candidate.getAppliedRole().name()
                                    : null;
                    return HRCandidateDashboardDto.builder()
                            .id(candidate.getId())
                            .username(candidate.getUsername())
                            .email(candidate.getEmail())
                            .phoneNumber(candidate.getPhoneNumber())
                            .panNumber(candidate.getPanNumber())
                            .aadhaarNumber(candidate.getAadhaarNumber())
                            .uanNumber(candidate.getEmployment() != null ? candidate.getEmployment().getUanNumber() : null)
                            .uanVerified(candidate.getEmployment() != null && Boolean.TRUE.equals(candidate.getEmployment().getUanVerified()))
                            .uanVerificationStatus(candidate.getEmployment() != null && candidate.getEmployment().getUanVerificationStatus() != null
                                    ? candidate.getEmployment().getUanVerificationStatus().name() : null)
                            .candidateType(candidate.getCandidateType() != null ? candidate.getCandidateType().name() : null)
                            .status(candidate.getApplicationStatus() != null ? candidate.getApplicationStatus().name() : "DRAFT")
                            .technicalSkills(candidate.getTechnicalSkills().stream().toList())
                            .appliedRole(appliedRole)
                            .build();
                })
                .toList();
    }
}