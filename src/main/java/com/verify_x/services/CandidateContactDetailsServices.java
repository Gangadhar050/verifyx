//package com.verify_x.services;
//
//import com.verify_x.dto.CandidateContactDetailsRequest1;
//import com.verify_x.dto.CandidateContactDetailsResponse1;
//
//public interface CandidateContactDetailsServices {
//
//    /**
//     * Create contact/address details
//     * for the logged-in candidate.
//     */
//    CandidateContactDetailsResponse1 createDetails(
//            CandidateContactDetailsRequest1 request
//    );
//
//    /**
//     * Update existing contact/address details
//     * for the logged-in candidate.
//     */
//    CandidateContactDetailsResponse1 updateDetails(
//            CandidateContactDetailsRequest1 request
//    );
//
//    /**
//     * Get contact/address details
//     * of the logged-in candidate.
//     */
//    CandidateContactDetailsResponse1 getMyDetails();
//
//    /**
//     * HR/Admin fetch candidate contact details.
//     */
//    CandidateContactDetailsResponse1 getDetailsByCandidateId(
//            Long candidateId
//    );
//}