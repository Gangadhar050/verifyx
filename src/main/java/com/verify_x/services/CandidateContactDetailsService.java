package com.verify_x.services;

import com.verify_x.dto.CandidateContactDetailsRequest;
import com.verify_x.dto.CandidateContactDetailsResponse;
import com.verify_x.dto.CurrentAddressRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface CandidateContactDetailsService {

    /**
     * Create permanent address + contact details.
     */
    CandidateContactDetailsResponse createDetails(
            CandidateContactDetailsRequest request
    );

    /**
     * Update permanent address + contact details.
     */
    CandidateContactDetailsResponse updateDetails(
            CandidateContactDetailsRequest request
    );

    /**
     * Capture/update current address using GPS + photo.
     */
    CandidateContactDetailsResponse updateCurrentAddress(
            CurrentAddressRequest request,
            MultipartFile photo
    );



    /**
     * Candidate gets own details.
     */
    CandidateContactDetailsResponse getMyDetails();

    /**
     * HR/Admin gets candidate details.
     */
    CandidateContactDetailsResponse getDetailsByCandidateId(
            Long candidateId
    );

    /**
     * Candidate/HR/Admin can view current address photo.
     */
    Resource getCurrentAddressPhoto(
            Long candidateId
    );
}