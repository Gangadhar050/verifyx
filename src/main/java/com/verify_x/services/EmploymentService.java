package com.verify_x.services;

import com.verify_x.dto.EmploymentDetailsDto;

import java.util.List;

public interface EmploymentService {

  /*
   * Create
   */
  void saveEmploymentDetails(EmploymentDetailsDto dto);

  /*
   * Update
   */
  void updateEmploymentDetails(EmploymentDetailsDto dto);

  /*
   * Candidate
   */
  EmploymentDetailsDto getEmploymentDetailsByCandidateId(Long candidateId);

  /*
   * Logged-in User
   */
  EmploymentDetailsDto getEmploymentDetails();

  /*
   * Search by Email
   */
  EmploymentDetailsDto getEmploymentDetailsByEmail(String email);

  /*
   * Search
   */
  List<EmploymentDetailsDto> searchEmploymentDetails(String keyword);

  /*
   * Delete
   */
  void deleteEmploymentDetails(Long candidateId);

}