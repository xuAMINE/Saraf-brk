package com.saraf.security.auth;

import com.saraf.security.config.JwtService;
import com.saraf.security.email.EmailService;
import com.saraf.security.token.TokenRepository;
import com.saraf.security.user.Role;
import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import com.saraf.security.user.VerTokenRepository;
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
    public void testRegisterUser() throws MessagingException {
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
    public void testResendEmailVerification() throws MessagingException {
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
}
