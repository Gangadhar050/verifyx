package com.verify_x.serviceImpl;


import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.dto.CandidateIdentityDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Employment;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EmploymentRepository;
import com.verify_x.services.CandidateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final EmploymentRepository employmentRepository;

    @Override
    public CandidateProfileDto getCandidateProfile(Long userId) {

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Candidate candidate = candidateRepository.findById(userId)
                .orElse(new Candidate());

        return CandidateProfileDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .appliedRole(user.getAppliedRole())
                .candidateType(user.getCandidateType())
                .address(candidate.getAddress())
                .panNumber(candidate.getPanNumber())
                .aadhaarNumber(candidate.getAadhaarNumber())

                .build();
    }

    @Override
    public CandidateProfileDto saveCandidateProfile(Long userId,
                                                    CandidateProfileDto dto) {

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (candidateRepository.existsByPanNumber(dto.getPanNumber())) {
            throw new RuntimeException("PAN Number already exists.");
        }

        if (candidateRepository.existsByAadhaarNumber(dto.getAadhaarNumber())) {
            throw new RuntimeException("Aadhaar Number already exists.");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAppliedRole(dto.getAppliedRole());
        user.setCandidateType(dto.getCandidateType());


        candidateRepository.save(user);

        Candidate candidate = Candidate.builder().build();
        user.setAddress(dto.getAddress());
        user.setPanNumber(dto.getPanNumber());
        user.setAadhaarNumber(dto.getAadhaarNumber());

        candidateRepository.save(user);


        log.info("Candidate profile created for User ID : {}", userId);

        return dto;
    }

    @Override
    public CandidateProfileDto updateCandidateProfile(Long userId,
                                                      CandidateProfileDto dto) {

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Candidate candidate = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
       // user.setAppliedRole(dto.getAppliedRole());
        user.setCandidateType(dto.getCandidateType());

        candidateRepository.save(user);

        candidate.setAddress(dto.getAddress());
        candidate.setPanNumber(dto.getPanNumber());
        candidate.setAadhaarNumber(dto.getAadhaarNumber());

        candidateRepository.save(candidate);

        log.info("Candidate profile updated for User ID : {}", userId);

        return dto;
    }

    @Override
    public CandidateIdentityDto updateCandidateIdentity(Long userId, CandidateIdentityDto dto) {
        Candidate candidate = candidateRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        String pan = dto.getPanNumber() == null ? null : dto.getPanNumber().trim().toUpperCase();
        String aadhaar = dto.getAadhaarNumber() == null ? null : dto.getAadhaarNumber().replaceAll("\\D", "");
        String uan = dto.getUanNumber() == null ? null : dto.getUanNumber().replaceAll("\\D", "");

        if (pan != null && !pan.isBlank()) {
            if (!pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]$")) {
                throw new RuntimeException("Invalid PAN Number");
            }
            candidate.setPanNumber(pan);
        }

        if (aadhaar != null && !aadhaar.isBlank()) {
            if (!aadhaar.matches("^\\d{12}$")) {
                throw new RuntimeException("Aadhaar Number must contain 12 digits");
            }
            candidate.setAadhaarNumber(aadhaar);
        }
        candidateRepository.save(candidate);

        if (uan != null && !uan.isBlank()) {
            if (!uan.matches("^\\d{12}$")) {
                throw new RuntimeException("UAN Number must contain 12 digits");
            }
            Employment employment = employmentRepository.findByCandidate(candidate)
                    .orElseGet(() -> Employment.builder().candidate(candidate).build());
            employment.setUanNumber(uan);
            employmentRepository.save(employment);
        }

        return CandidateIdentityDto.builder()
                .panNumber(candidate.getPanNumber())
                .aadhaarNumber(candidate.getAadhaarNumber())
                .uanNumber(uan)
                .build();
    }

    @Override
    public void saveCandidateProfile(Candidate candidate) {

        candidateRepository.save(candidate);

        log.info("Candidate profile created for User ID : {}", candidate.getId());
    }

}
