package com.verify_x.services;

import com.verify_x.dto.HrOfferLetterRequestDTO;
import com.verify_x.dto.HrOfferLetterResponseDTO;

import java.util.List;

public interface HrOfferLetterService {

    HrOfferLetterResponseDTO sendOfferLetter(
            Long candidateId,
            HrOfferLetterRequestDTO request,
            String sentBy);

    List<HrOfferLetterResponseDTO> getSentOfferLetters(Long candidateId);
}