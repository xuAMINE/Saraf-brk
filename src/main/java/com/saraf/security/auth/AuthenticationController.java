package com.saraf.security.auth;

import com.saraf.security.config.JwtService;
import com.saraf.security.email.ResendVerificationRequest;
import com.saraf.security.exception.EmailValidationException;
import com.saraf.security.user.Role;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

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
      return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
    }
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
    AuthenticationResponse response = service.authenticate(request);

    if (response.getMessage() != null && response.getMessage().equals("Email not verified")) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } else if (response.getMessage() != null && response.getMessage().equals("Invalid credentials")) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    service.refreshToken(request, response);
  }

  @GetMapping("/activate-account")
  public ResponseEntity<String> activateAccount(@RequestParam String verToken) throws MessagingException {
    try {
      boolean isActivated = service.activateAccount(verToken);
      if (isActivated) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://sarafbrk.com/account-activated/"))
                .build();
      } else
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to activate account. Please check your token or request a new one.");

    } catch (EmailValidationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to activate account");
    }
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest request) throws MessagingException, UnsupportedEncodingException {
    service.resendEmailVerification(request.getEmail());
    return ResponseEntity.ok("Verification email resent successfully.");
  }

  @GetMapping("/check-session")
  public ResponseEntity<?> checkSession(@RequestHeader("Authorization") String token) {
    String jwt = token.replace("Bearer ", "");
    Role role = jwtService.getUserRoleFromToken(jwt);
    if (jwtService.isTokenValid(jwt)) {
      if (role.equals(Role.ADMIN) || role.equals(Role.MANAGER))
        return ResponseEntity.ok("Admin session valid");
      return ResponseEntity.ok("User session valid");
    } else {
      return ResponseEntity.status(401).body(false);
    }
  }
}