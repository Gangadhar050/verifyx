package com.verify_x.serviceImpl;

import com.verify_x.dto.EmploymentDetailsDto;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Employment;
import com.verify_x.enums.EmploymentStatus;
import com.verify_x.enums.NoticePeriodStatus;
import com.verify_x.enums.VerificationStatus;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EmploymentRepository;
import com.verify_x.services.EmploymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmploymentServiceImpl implements EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final CandidateRepository candidateRepository;

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Candidate not found."));
    }

    private void validateEmployment(EmploymentDetailsDto dto) {

        if (dto.getEmploymentStatus() == null) {

            throw new BadRequestException(
                    "Employment Status is required.");
        }

        if (dto.getEmploymentStatus() ==
                EmploymentStatus.CURRENTLY_EMPLOYED) {

            validateCurrentEmployment(dto);

        } else {

            validatePreviousEmployment(dto);
        }

    }

    private void validateCurrentEmployment(
            EmploymentDetailsDto dto) {

        if (isBlank(dto.getCurrentCompany())) {

            throw new BadRequestException(
                    "Current Company is required.");
        }

        if (isBlank(dto.getCurrentDesignation())) {

            throw new BadRequestException(
                    "Current Designation is required.");
        }

        if (dto.getCurrentCTC() == null) {

            throw new BadRequestException(
                    "Current CTC is required.");
        }

        if (dto.getWorkingFrom() == null) {

            throw new BadRequestException(
                    "Working From date is required.");
        }

        if (dto.getNoticePeriodStatus() == null) {

            throw new BadRequestException(
                    "Notice Period is required.");
        }

        if (dto.getNoticePeriodStatus()
                == NoticePeriodStatus.YES) {

            if (dto.getLastWorkingDate() == null) {

                throw new BadRequestException(
                        "Last Working Date is required.");
            }

        } else {

            if (dto.getOfficialNoticePeriod() == null) {

                throw new BadRequestException(
                        "Official Notice Period is required.");
            }

        }

    }

    private void validatePreviousEmployment(
            EmploymentDetailsDto dto) {

        if (isBlank(dto.getPreviousCompanyName())) {

            throw new BadRequestException(
                    "Previous Company is required.");
        }

        if (isBlank(dto.getPreviousDesignation())) {

            throw new BadRequestException(
                    "Previous Designation is required.");
        }

        if (dto.getPreviousWorkingFrom() == null) {

            throw new BadRequestException(
                    "Previous Working From is required.");
        }

        if (dto.getPreviousWorkingTo() == null) {

            throw new BadRequestException(
                    "Previous Working To is required.");
        }

        if (dto.getPreviousCTC() == null) {

            throw new BadRequestException(
                    "Previous CTC is required.");
        }

        if (dto.getExpectedCTC() == null) {

            throw new BadRequestException(
                    "Expected CTC is required.");
        }

        if (dto.getTotalExperience() == null) {

            throw new BadRequestException(
                    "Experience is required.");
        }

        if (isBlank(dto.getUanNumber())) {

            throw new BadRequestException(
                    "UAN Number is required.");
        }

    }

    private boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();

    }

    private void mapDtoToEntity(
            Employment employment,
            EmploymentDetailsDto dto) {

        employment.setEmploymentStatus(
                dto.getEmploymentStatus());

        if (dto.getEmploymentStatus() ==
                EmploymentStatus.CURRENTLY_EMPLOYED) {

            employment.setCurrentCompany(
                    dto.getCurrentCompany());

            employment.setCurrentDesignation(
                    dto.getCurrentDesignation());

            employment.setCurrentCTC(
                    dto.getCurrentCTC());

            employment.setWorkingFrom(
                    dto.getWorkingFrom());

            employment.setNoticePeriodStatus(
                    dto.getNoticePeriodStatus());

            employment.setOfficialNoticePeriod(
                    dto.getOfficialNoticePeriod());

            employment.setLastWorkingDate(
                    dto.getLastWorkingDate());

            /*
             * Clear Previous Details
             */

            employment.setPreviousCompanyName(null);
            employment.setPreviousDesignation(null);
            employment.setPreviousWorkingFrom(null);
            employment.setPreviousWorkingTo(null);
            employment.setPreviousCTC(null);
            employment.setExpectedCTC(null);
            employment.setTotalExperience(null);
            employment.setUanNumber(null);

        } else {

            employment.setPreviousCompanyName(
                    dto.getPreviousCompanyName());

            employment.setPreviousDesignation(
                    dto.getPreviousDesignation());

            employment.setPreviousWorkingFrom(
                    dto.getPreviousWorkingFrom());

            employment.setPreviousWorkingTo(
                    dto.getPreviousWorkingTo());

            employment.setPreviousCTC(
                    dto.getPreviousCTC());

            employment.setExpectedCTC(
                    dto.getExpectedCTC());

            employment.setTotalExperience(
                    dto.getTotalExperience());

            employment.setUanNumber(
                    dto.getUanNumber());

            /*
             * Clear Current Details
             */

            employment.setCurrentCompany(null);
            employment.setCurrentDesignation(null);
            employment.setCurrentCTC(null);
            employment.setWorkingFrom(null);
            employment.setNoticePeriodStatus(null);
            employment.setOfficialNoticePeriod(null);
            employment.setLastWorkingDate(null);

        }

    }
    private EmploymentDetailsDto mapEntityToDto(Employment employment) {

        return EmploymentDetailsDto.builder()

                .employmentStatus(employment.getEmploymentStatus())

                /*
                 * Current Employment
                 */

                .currentCompany(employment.getCurrentCompany())

                .currentDesignation(employment.getCurrentDesignation())

                .currentCTC(employment.getCurrentCTC())

                .workingFrom(employment.getWorkingFrom())

                /*
                 * Notice Period
                 */

                .noticePeriodStatus(employment.getNoticePeriodStatus())

                .officialNoticePeriod(
                        employment.getOfficialNoticePeriod())

                .lastWorkingDate(
                        employment.getLastWorkingDate())

                /*
                 * Previous Employment
                 */

                .previousCompanyName(
                        employment.getPreviousCompanyName())

                .previousDesignation(
                        employment.getPreviousDesignation())

                .previousWorkingFrom(
                        employment.getPreviousWorkingFrom())

                .previousWorkingTo(
                        employment.getPreviousWorkingTo())

                .previousCTC(
                        employment.getPreviousCTC())

                .expectedCTC(
                        employment.getExpectedCTC())

                .totalExperience(
                        employment.getTotalExperience())

                /*
                 * UAN
                 */

                .uanNumber(
                        employment.getUanNumber())

                .build();

    }
    @Override
    public void saveEmploymentDetails(EmploymentDetailsDto dto) {

        Candidate candidate = getLoggedInCandidate();

        /*
         * Prevent Duplicate Employment Record
         */

        if (employmentRepository.existsByCandidate(candidate)) {

            throw new BadRequestException(
                    "Employment details already exist. Please use Update.");
        }

        /*
         * Validate Business Rules
         */

        validateEmployment(dto);

        /*
         * Create Entity
         */

        Employment employment = new Employment();

        employment.setCandidate(candidate);

        /*
         * Map DTO → Entity
         */

        mapDtoToEntity(employment, dto);

        /*
         * Default UAN Verification
         */

        employment.setUanVerified(false);

        /*
         * Save
         */
        if (dto.getPreviousWorkingFrom() != null

                && dto.getPreviousWorkingTo() != null
                && dto.getPreviousWorkingTo()
                .isBefore(dto.getPreviousWorkingFrom())) {

            throw new BadRequestException(
                    "Previous Working To cannot be before Previous Working From.");
        }
        if (dto.getWorkingFrom() != null
                && dto.getLastWorkingDate() != null
                && dto.getLastWorkingDate()
                .isBefore(dto.getWorkingFrom())) {

            throw new BadRequestException(
                    "Last Working Date cannot be before Working From.");
        }
        employmentRepository.save(employment);

        log.info(
                "Employment details created successfully for candidate {}",
                candidate.getEmail());

    }

    @Override
    public void updateEmploymentDetails(EmploymentDetailsDto dto) {

        /*
         * Logged-in Candidate
         */
        Candidate candidate = getLoggedInCandidate();

        /*
         * Existing Employment
         */
        Employment employment =
                employmentRepository.findByCandidate(candidate)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found."
                                ));

        /*
         * Validate
         */
        validateEmployment(dto);

        /*
         * Additional Date Validation
         */

        if (dto.getEmploymentStatus() ==
                EmploymentStatus.CURRENTLY_EMPLOYED) {

            if (dto.getWorkingFrom() != null &&
                    dto.getLastWorkingDate() != null &&
                    dto.getLastWorkingDate()
                            .isBefore(dto.getWorkingFrom())) {

                throw new BadRequestException(
                        "Last Working Date cannot be before Working From.");
            }

        } else {

            if (dto.getPreviousWorkingFrom() != null &&
                    dto.getPreviousWorkingTo() != null &&
                    dto.getPreviousWorkingTo()
                            .isBefore(dto.getPreviousWorkingFrom())) {

                throw new BadRequestException(
                        "Previous Working To cannot be before Previous Working From.");
            }
        }

        /*
         * Update Entity
         */

        mapDtoToEntity(employment, dto);

        /*
         * Save
         */
        if (dto.getEmploymentStatus() == EmploymentStatus.NOT_CURRENTLY_EMPLOYED) {

            if (!java.util.Objects.equals(
                    employment.getUanNumber(),
                    dto.getUanNumber())) {

                employment.setUanVerified(false);

                employment.setUanVerifiedBy(null);

                employment.setUanVerifiedAt(null);

                employment.setUanVerificationStatus(
                        VerificationStatus.PENDING);
            }
        }
        employmentRepository.save(employment);

        log.info(
                "Employment details updated successfully for candidate {}",
                candidate.getEmail());

    }
    @Override
    public EmploymentDetailsDto getEmploymentDetails() {

        Candidate candidate = getLoggedInCandidate();

        Employment employment =
                employmentRepository.findByCandidate(candidate)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found."
                                ));

        return mapEntityToDto(employment);

    }@Override
    public EmploymentDetailsDto getEmploymentDetailsByCandidateId(
            Long candidateId) {

        Candidate candidate =
                candidateRepository.findById(candidateId)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));

        Employment employment =
                employmentRepository.findByCandidate(candidate)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found."
                                ));

        return mapEntityToDto(employment);

    }@Override
    public EmploymentDetailsDto getEmploymentDetailsByEmail(
            String email) {

        Candidate candidate =
                candidateRepository.findByEmail(email)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate with email "
                                                + email
                                                + " not found."
                                ));

        Employment employment =
                employmentRepository.findByCandidate(candidate)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found."
                                ));

        return mapEntityToDto(employment);

    }
    @Override
    public List<EmploymentDetailsDto> searchEmploymentDetails(String keyword) {

        List<Employment> employments =
                employmentRepository.findAll();

        return employments.stream()

                .filter(e ->

                        contains(e.getCurrentCompany(), keyword)

                                || contains(e.getPreviousCompanyName(), keyword)

                                || contains(e.getCurrentDesignation(), keyword)

                                || contains(e.getPreviousDesignation(), keyword)

                                || contains(e.getUanNumber(), keyword)

                                || (e.getCandidate() != null &&
                                contains(e.getCandidate().getUsername(), keyword))

                                || (e.getCandidate() != null &&
                                contains(e.getCandidate().getEmail(), keyword))

                )

                .map(this::mapEntityToDto)

                .toList();

    }
    private boolean contains(String value, String keyword) {

        if (value == null || keyword == null) {
            return false;
        }

        return value.toLowerCase()
                .contains(keyword.toLowerCase());

    }
    @Override
    public void deleteEmploymentDetails(Long candidateId) {

        Candidate candidate =
                candidateRepository.findById(candidateId)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId));

        Employment employment =
                employmentRepository.findByCandidate(candidate)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found."
                                ));

        employmentRepository.delete(employment);

        log.info(
                "Employment details deleted successfully for candidate {}",
                candidate.getEmail());

    }}


