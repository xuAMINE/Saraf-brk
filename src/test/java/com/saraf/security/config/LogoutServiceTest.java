package com.saraf.security.config;

import com.saraf.security.token.Token;
import com.saraf.security.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logout_ValidJwt_Success() {
        String jwt = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);

        Token storedToken = new Token();
        storedToken.setToken(jwt);
        storedToken.setExpired(false);
        storedToken.setRevoked(false);

        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.of(storedToken));

        logoutService.logout(request, response, authentication);

        assertTrue(storedToken.isExpired());
        assertTrue(storedToken.isRevoked());
        verify(tokenRepository, times(1)).save(storedToken);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_MissingAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(anyString());
        verify(tokenRepository, never()).save(any(Token.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_InvalidJwtPrefix() {
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix jwt-token");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(anyString());
        verify(tokenRepository, never()).save(any(Token.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
