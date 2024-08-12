package com.saraf.security.auth;

import com.saraf.security.config.JwtService;
import com.saraf.security.email.ResendVerificationRequest;
import com.saraf.security.exception.InvalidTokenException;
import com.saraf.security.exception.TokenExpiredException;
import com.saraf.security.user.User;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthenticationController {

  private final AuthenticationService service;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
    try {
      if (service.userExists(request.getEmail())) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
      }
      return ResponseEntity.ok(service.register(request));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
    }
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
    AuthenticationResponse response = service.authenticate(request);

    if (response.getMessage() != null && response.getMessage().equals("Email not verified")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    } else if (response.getMessage() != null && response.getMessage().equals("Invalid credentials")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    service.refreshToken(request, response);
  }

  @GetMapping("/activate-account")
  public ResponseEntity<String> activateAccount(@RequestParam String verToken, HttpServletResponse response) throws MessagingException {
    try {
      boolean isActivated = service.activateAccount(verToken);
      if (isActivated)
        response.sendRedirect("http://127.0.0.1:5501/Saraf-BRK/pages/account-activated.html");
      return ResponseEntity.ok("Account activated successfully");

    } catch (InvalidTokenException | TokenExpiredException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to activate account");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest request) throws MessagingException {
    service.resendEmailVerification(request.getEmail());
    return ResponseEntity.ok("Verification email resent successfully.");
  }

  @GetMapping("/check-session")
  public ResponseEntity<?> checkSession(@RequestHeader("Authorization") String token) {
    String jwt = token.replace("Bearer ", "");
    if (jwtService.isTokenValid(jwt)) {
      return ResponseEntity.ok(true);
    } else {
      return ResponseEntity.status(401).body(false);
    }
  }

}