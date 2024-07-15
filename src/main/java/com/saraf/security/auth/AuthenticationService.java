package com.saraf.security.auth;

import com.saraf.security.config.JwtService;
import com.saraf.security.email.EmailTemplateName;
import com.saraf.security.email.emailService;
import com.saraf.security.exception.InvalidTokenException;
import com.saraf.security.exception.TokenExpiredException;
import com.saraf.security.token.Token;
import com.saraf.security.token.TokenRepository;
import com.saraf.security.token.TokenType;
import com.saraf.security.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final VerTokenRepository verTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final com.saraf.security.email.emailService emailService;

  @Value("${application.mailing.frontend.activation-url}")
  private String activationUrl;

  public AuthenticationResponse register(RegisterRequest request) throws MessagingException {
    var user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();
    var savedUser = repository.save(user);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    sendValidationEmail(user);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
        .build();
  }

  private void sendValidationEmail(User user) throws MessagingException {
    var newToken = generateAndSaveActivationToken(user);

    emailService.sendEmail(
            user.getEmail(),
            user.getFullName(),
            EmailTemplateName.ACTIVATE_ACCOUNT,
            activationUrl+newToken,
            newToken,
            "Account activation"
    );
  }

  public void resendEmailVerification(String email) throws MessagingException {
    var user = repository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email));

    if (user.isEnabled())
      throw new IllegalStateException("Email is already verified");

    // Invalidate existing tokens
    invalidateUserVerTokens(user);
    sendValidationEmail(user);
  }

  private void invalidateUserVerTokens(User user) {
    List<VerToken> activeTokens = verTokenRepository.findAllActiveTokensByUser(user.getId());

    for (VerToken verToken : activeTokens) {
      verToken.markAsExpired();
      verTokenRepository.save(verToken);
    }
  }

  private String generateAndSaveActivationToken(User user) {
    String generatedToken = generateActivationCode(6);
    var verToken = VerToken.builder()
            .token(generatedToken)
            .created(LocalDateTime.now())
            .expires(LocalDateTime.now().plusMinutes(15))
            .user(user)
            .build();
    verTokenRepository.save(verToken);

    return generatedToken;
  }

  private String generateActivationCode(int length) {
    String characters = "0123456789";
    StringBuilder codeBuilder = new StringBuilder();
    SecureRandom random = new SecureRandom();

    for (int i = 0; i < length; i++) {
      int randomChar = random.nextInt(characters.length()); // 0..9
      codeBuilder.append(characters.charAt(randomChar));
    }

    return codeBuilder.toString();
  }


  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
      );
    } catch (DisabledException e) {
      // Handle disabled (not verified) user account
      return AuthenticationResponse.builder()
              .message("Email not verified")
              .build();
    } catch (AuthenticationException e) {
      // Handle other authentication exceptions
      return AuthenticationResponse.builder()
              .message("Invalid credentials")
              .build();
    }

    var user = repository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

  @Transactional
  public void activateAccount(String token) throws MessagingException {
    if (token == null || token.isEmpty()) {
      throw new IllegalArgumentException("Token must not be empty");
    }

    VerToken savedToken = verTokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid token"));

    if (!savedToken.isActive()) {
      resendEmailVerification(savedToken.getUser().getEmail());
      throw new TokenExpiredException("token expired");
    }

    var user = repository.findById(savedToken.getUser().getId())
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));

    user.setEnabled(true);

    repository.save(user);
    savedToken.setValidatedAt(LocalDateTime.now());
    verTokenRepository.save(savedToken);
  }
}
