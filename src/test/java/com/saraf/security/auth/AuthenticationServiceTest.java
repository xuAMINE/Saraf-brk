package com.saraf.security.auth;

import com.saraf.security.config.JwtService;
import com.saraf.security.email.EmailService;
import com.saraf.security.exception.EmailValidationException;
import com.saraf.security.token.Token;
import com.saraf.security.token.TokenRepository;
import com.saraf.security.user.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private VerTokenRepository verTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser() throws MessagingException, UnsupportedEncodingException {
        RegisterRequest request = new RegisterRequest();
        request.setFirstname("John");
        request.setLastname("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("password");

        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getAccessToken());
    }

    @Test
    public void testAuthenticateUser() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("password");

        User user = User.builder()
                .email("john.doe@example.com")
                .password("password")
                .enabled(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getAccessToken());
    }

    @Test
    public void testRefreshToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        User user = User.builder().email("test@test.com").enabled(true).build();

        when(request.getHeader("Authorization")).thenReturn("Bearer refreshToken");
        when(jwtService.extractUsername("refreshToken")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refreshToken", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newAccessToken");

        // Mock the OutputStream to avoid IllegalArgumentException
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        authenticationService.refreshToken(request, response);

        verify(jwtService, times(1)).generateToken(user);
        verify(tokenRepository, times(1)).save(any());
    }


    @Test
    public void testResendEmailVerification() throws MessagingException, UnsupportedEncodingException {
        User user = User.builder().email("test@test.com").enabled(false).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        authenticationService.resendEmailVerification("test@test.com");

        verify(verTokenRepository, times(1)).findAllActiveTokensByUser(user.getId());
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    public void testResendEmailVerification_AlreadyVerified() {
        User user = User.builder().email("test@test.com").enabled(true).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> authenticationService.resendEmailVerification("test@test.com"));
    }

    @Test
    public void testUserExists() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        boolean userExists = authenticationService.userExists("test@test.com");

        assertTrue(userExists);
    }

    @Test
    public void testInvalidateUserVerTokens() {
        User user = User.builder().id(1).build();
        VerificationToken token = VerificationToken.builder().token("validToken").build();
        List<VerificationToken> activeTokens = List.of(token);

        when(verTokenRepository.findAllActiveTokensByUser(user.getId())).thenReturn(activeTokens);

        authenticationService.invalidateUserVerTokens(user);

        verify(verTokenRepository, times(1)).save(any(VerificationToken.class));
        assertTrue(token.isExpired()); // Ensure the token is marked as expired
    }

    @Test
    public void testGenerateAndSaveActivationToken() {
        User user = User.builder().id(1).build();

        when(verTokenRepository.save(any(VerificationToken.class))).thenReturn(null); // Mocking save

        String token = authenticationService.generateAndSaveActivationToken(user);

        assertNotNull(token);
        assertEquals(6, token.length()); // Ensure the generated token is of the correct length
        verify(verTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    public void testGenerateActivationCode() {
        String activationCode = authenticationService.generateActivationCode(6);

        assertNotNull(activationCode);
        assertEquals(6, activationCode.length());
        assertTrue(activationCode.matches("[0-9]+")); // Check if it contains only digits
    }

    @Test
    public void testSaveUserToken() {
        User user = User.builder().id(1).email("test@test.com").build();
        String jwtToken = "testToken";

        authenticationService.saveUserToken(user, jwtToken);

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testRevokeAllUserTokens() {
        User user = User.builder().id(1).build();
        Token validToken = Token.builder().token("validToken").build();
        List<Token> validTokens = List.of(validToken);

        when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(validTokens);

        authenticationService.revokeAllUserTokens(user);

        verify(tokenRepository, times(1)).saveAll(anyList());
        assertTrue(validToken.isExpired());
        assertTrue(validToken.isRevoked());
    }

    @Test
    public void testActivateAccount_Success() throws MessagingException {
        String token = "validToken";
        User user = User.builder().id(1).enabled(false).build();
        VerificationToken verificationToken = VerificationToken.builder().token(token).user(user).expires(LocalDateTime.now().plusMinutes(10)).build();

        when(verTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        boolean result = authenticationService.activateAccount(token);

        assertTrue(result);
        verify(userRepository, times(1)).save(user);
        assertTrue(user.isEnabled());
    }

    @Test
    public void testActivateAccount_TokenExpired() {
        String token = "expiredToken";
        User user = User.builder().id(1).enabled(false).build();
        VerificationToken expiredToken = VerificationToken.builder().token(token).user(user).expires(LocalDateTime.now().minusMinutes(1)).build();

        when(verTokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        assertThrows(EmailValidationException.class, () -> authenticationService.activateAccount(token));
    }

    @Test
    public void testActivateAccount_InvalidToken() {
        String token = "invalidToken";

        when(verTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(EmailValidationException.class, () -> authenticationService.activateAccount(token));
    }

    @Test
    public void testActivateAccount_AlreadyActivated() {
        String token = "validToken";
        User user = User.builder().id(1).enabled(true).build();
        VerificationToken verificationToken = VerificationToken.builder().token(token).user(user).expires(LocalDateTime.now().plusMinutes(10)).build();

        when(verTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(EmailValidationException.class, () -> authenticationService.activateAccount(token));
    }
}
