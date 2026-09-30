package com.verify_x.services;

import com.verify_x.dto.InterviewCandidateDto;

import java.util.List;

public interface InterviewService {

    List<InterviewCandidateDto> getInterviewCandidates();

    void confirmInterview(
            Long candidateId,
            String confirmedBy);

    void approveInterview(
            Long candidateId,
            String remarks);

    void rejectInterview(
            Long candidateId,
            String remarks);

}