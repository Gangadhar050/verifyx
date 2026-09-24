package com.verify_x.services;

import com.verify_x.dto.CurrentAddressRequest;
import com.verify_x.dto.CurrentAddressResponse;
import com.verify_x.dto.PermanentAddressRequest;
import com.verify_x.dto.PermanentAddressResponse;

public interface CandidateContactDetailsService {

    PermanentAddressResponse createPermanentAddress(PermanentAddressRequest request);

    PermanentAddressResponse updatePermanentAddress(PermanentAddressRequest request);

    PermanentAddressResponse getMyPermanentAddress();

    CurrentAddressResponse createOrUpdateCurrentAddress(CurrentAddressRequest request);

    CurrentAddressResponse getMyCurrentAddress();
    // HR / ADMIN
    PermanentAddressResponse getPermanentAddressByCandidateId(Long candidateId);

    CurrentAddressResponse getCurrentAddressByCandidateId(Long candidateId);
}