package com.saraf.security.user;

import com.saraf.security.user.forgot_password.ForgotPasswordRequest;
import com.saraf.security.user.forgot_password.ResetPasswordRequest;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, Principal connectedUser) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
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
}
