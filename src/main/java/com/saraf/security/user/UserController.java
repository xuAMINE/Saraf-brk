package com.saraf.security.user;

import com.saraf.security.user.forgot_password.ForgotPasswordRequest;
import com.saraf.security.user.forgot_password.ResetPasswordRequest;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PatchMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, Principal connectedUser) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-phone")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<?> updatePhoneNumber(Principal connectedUser, @RequestParam("phoneNumber") String phoneNumber) {
        try {
            service.updatePhoneNumber(connectedUser, phoneNumber);
            return ResponseEntity.ok("Phone number updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating phone number.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) throws MessagingException {
        service.forgotPassword(request);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String token,
                                              @RequestBody @Valid ResetPasswordRequest request) {
        service.resetPassword(token, request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/has-phone")
    public ResponseEntity<Boolean> checkUserPhone(Principal connectedUser) {
        return ResponseEntity.ok(service.hasPhoneNumber(connectedUser));
    }


}
