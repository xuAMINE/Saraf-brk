package com.saraf.security.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.saraf.security.email.EmailService;
import com.saraf.security.exception.PasswordResetException;
import com.saraf.security.user.forgot_password.PasswordResetToken;
import com.saraf.security.user.forgot_password.PasswordResetTokenRepository;
import com.saraf.security.user.forgot_password.ForgotPasswordRequest;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private PasswordResetToken resetToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password("encoded_password")
                .enabled(true)
                .build();

        resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(testUser);
        resetToken.setExpirationTime(LocalDateTime.now().plusMinutes(15));
    }

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("current_password", "new_password", "new_password");
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(passwordEncoder.matches("current_password", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new_password");

        userService.changePassword(request, principal);

        verify(userRepository).save(testUser);
        assertThat(testUser.getPassword()).isEqualTo("encoded_new_password");
    }

    @Test
    void testChangePassword_IncorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong_password", "new_password", "new_password");
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(passwordEncoder.matches("wrong_password", testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(request, principal))
                .isInstanceOf(PasswordResetException.class)
                .hasMessage("Incorrect password");
    }

    @Test
    void testForgotPassword_Success() throws MessagingException, MessagingException, UnsupportedEncodingException {
        ForgotPasswordRequest request = new ForgotPasswordRequest("johndoe@example.com");
        when(userRepository.findByEmail("johndoe@example.com")).thenReturn(Optional.of(testUser));

        userService.forgotPassword(request);

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokenRepository).save(captor.capture());

        PasswordResetToken savedToken = captor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
    }

    @Test
    void testForgotPassword_UserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.forgotPassword(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not found");
    }

    @Test
    void testResetPassword_Success() {
        when(resetTokenRepository.findByToken(resetToken.getToken())).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new_password");

        userService.resetPassword(resetToken.getToken(), "new_password", "new_password");

        verify(userRepository).save(testUser);
        verify(resetTokenRepository).delete(resetToken);
        assertThat(testUser.getPassword()).isEqualTo("encoded_new_password");
    }

    @Test
    void testResetPassword_TokenExpired() {
        resetToken.setExpirationTime(LocalDateTime.now().minusMinutes(1));
        when(resetTokenRepository.findByToken(resetToken.getToken())).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> userService.resetPassword(resetToken.getToken(), "new_password", "new_password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Session is expired, Please make a new request.");
    }

    @Test
    void testResetPassword_PasswordsDoNotMatch() {
        when(resetTokenRepository.findByToken(resetToken.getToken())).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> userService.resetPassword(resetToken.getToken(), "new_password", "different_password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Passwords do not match.");
    }

    @Test
    void testUpdatePhoneNumber_Success() {
        String newPhoneNumber = "+1234567890";
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        userService.updatePhoneNumber(principal, newPhoneNumber);

        verify(userRepository).save(testUser);
        assertThat(testUser.getPhoneNumber()).isEqualTo(newPhoneNumber);
    }

    @Test
    void testUpdatePhoneNumber_InvalidPhoneNumber() {
        String invalidPhoneNumber = "123"; // Invalid format
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertThatThrownBy(() -> userService.updatePhoneNumber(principal, invalidPhoneNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid phone number format");
    }

    @Test
    void testHasPhoneNumber_PhoneNumberExists() {
        testUser.setPhoneNumber("+1234567890");
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        boolean result = userService.hasPhoneNumber(principal);

        assertThat(result).isTrue();
    }

    @Test
    void testHasPhoneNumber_NoPhoneNumber() {
        testUser.setPhoneNumber(null);
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        boolean result = userService.hasPhoneNumber(principal);

        assertThat(result).isFalse();
    }
}
