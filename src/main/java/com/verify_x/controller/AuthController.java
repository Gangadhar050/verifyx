package com.verify_x.controller;

import com.verify_x.dto.AdminLoginRequestDto;
import com.verify_x.dto.CurrentUserDto;
import com.verify_x.dto.ForgotPasswordRequestDto;
import com.verify_x.dto.HrLoginResponseDto;
import com.verify_x.dto.LoginRequestDto;
import com.verify_x.dto.LoginResponseDto;
import com.verify_x.dto.ResetPasswordRequestDto;
import com.verify_x.dto.UserRegistrationDto;
import com.verify_x.dto.VerifyOtpRequestDto;
import com.verify_x.entity.Admin;
import com.verify_x.payload.ApiResponse;
import com.verify_x.repository.AdminRepository;
import com.verify_x.services.AuthService;
import com.verify_x.services.EmailService;
import com.verify_x.enums.AppliedRole;
import com.verify_x.enums.CandidateType;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AuthController {

    private final AuthService authService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Map<String, HrResetOtp> hrResetOtps = new ConcurrentHashMap<>();

    private record HrResetOtp(String hash, LocalDateTime expiresAt) {}

    @PostMapping("/candidateRegister")
    public ResponseEntity<ApiResponse<String>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto) {

        String response = authService.register(registrationDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<String>builder()
                                .success(true)
                                .message("User registered successfully.")
                                .data(response)
                                .build()
                );
    }

    @GetMapping("/registration-options")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> registrationOptions() {
        java.util.Map<String, Object> options = new java.util.LinkedHashMap<>();
        options.put("appliedRoles", java.util.Arrays.stream(AppliedRole.values())
                .map(role -> java.util.Map.of("value", role.name(), "label", displayLabel(role.name())))
                .toList());
        options.put("candidateTypes", java.util.Arrays.stream(CandidateType.values())
                .map(type -> java.util.Map.of("value", type.name(), "label", displayLabel(type.name())))
                .toList());
        return ResponseEntity.ok(ApiResponse.<java.util.Map<String, Object>>builder()
                .success(true).message("Registration options fetched successfully.").data(options).build());
    }

    private String displayLabel(String value) {
        return java.util.Arrays.stream(value.toLowerCase().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    @PostMapping("/candidateLogin")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {

        LoginResponseDto response =
                authService.login(loginRequestDto);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponseDto>builder()
                        .success(true)
                        .message("Login successful.")
                        .data(response)
                        .build()
        );
    }

    //current user logged in
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserDto>> currentUser() {

        return ResponseEntity.ok(
                ApiResponse.<CurrentUserDto>builder()
                        .success(true)
                        .message("Current user fetched successfully.")
                        .data(authService.getCurrentUser())
                        .build()
        );
    }

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<ApiResponse<String>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyOtpRequestDto request) {

        String response =
                authService.verifyRegistrationOtp(request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("OTP verification successful.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/candidate/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyCandidateEmailCompat(@RequestBody Map<String, String> body) {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setEmail(body.get("email"));
        request.setEmailOtp(body.getOrDefault("emailOtp", body.get("otp")));
        String response = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true).message("OTP verification successful.").data(response).build());
    }

    @PostMapping("/candidate/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendCandidateVerification(@RequestParam String email) {
        String response = authService.resendRegistrationOtp(email);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true).message("Verification OTP resent successfully.").data(response).build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        String response =
                authService.initiatePasswordReset(
                        request.getEmail()
                );

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("If the email exists, an OTP has been sent.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        String response =
                authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Password reset successful.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/candidateLogout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new BadCredentialsException(
                    "Invalid Authorization header."
            );
        }

        String token = authHeader.substring(7);

        authService.logout(token);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Logout successful.")
                        .data("Token invalidated successfully.")
                        .build()
        );
    }

    @PostMapping("/hr/forgot-password")
    public ResponseEntity<ApiResponse<String>> hrForgotPassword(@RequestParam String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("HR user not found."));
        String otp = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        hrResetOtps.put(admin.getEmail().toLowerCase(), new HrResetOtp(passwordEncoder.encode(otp), LocalDateTime.now().plusMinutes(10)));
        emailService.sendOtp(admin.getEmail(), otp);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("HR reset OTP sent.").data("OTP sent to email.").build());
    }

    @PostMapping("/hr/reset-password")
    public ResponseEntity<ApiResponse<String>> hrResetPassword(@RequestBody Map<String, String> body) {
        String email = String.valueOf(body.getOrDefault("email", "")).trim().toLowerCase();
        String otp = body.getOrDefault("emailOtp", body.get("otp"));
        String newPassword = body.get("newPassword");
        HrResetOtp saved = hrResetOtps.get(email);
        if (saved == null || saved.expiresAt().isBefore(LocalDateTime.now()) || otp == null || !passwordEncoder.matches(otp, saved.hash())) {
            throw new BadCredentialsException("Invalid or expired HR reset OTP.");
        }
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("New password is required.");
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("HR user not found."));
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        hrResetOtps.remove(email);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("HR password reset successful.").data("Password updated.").build());
    }

    @PostMapping("/hrLogin")
    public ResponseEntity<ApiResponse<HrLoginResponseDto>> adminLogin(
            @Valid @RequestBody AdminLoginRequestDto request) {

        HrLoginResponseDto response =
                authService.adminLogin(request);

        return ResponseEntity.ok(
                ApiResponse.<HrLoginResponseDto>builder()
                        .success(true)
                        .message("Admin login successful.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/hrLogout")
    public ResponseEntity<ApiResponse<String>> adminLogout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new BadCredentialsException(
                    "Invalid Authorization header."
            );
        }

        String token = authHeader.substring(7);

        authService.adminLogout(token);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Admin logged out successfully.")
                        .data("Logout successful.")
                        .build()
        );
    }
}