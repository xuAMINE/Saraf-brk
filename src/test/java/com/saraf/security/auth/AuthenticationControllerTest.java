package com.saraf.security.auth;

import com.saraf.security.config.JwtService;
import com.saraf.security.email.ResendVerificationRequest;
import com.saraf.security.exception.EmailValidationException;
import com.saraf.security.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @Test
    void activateAccount_SuccessfulActivation() throws Exception {
        String token = "validToken";

        // Mock the service method to return true for successful activation
        when(authenticationService.activateAccount(token)).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/activate-account")
                        .param("verToken", token))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://127.0.0.1:5501/Saraf-BRK/pages/account-activated.html"));
    }

    @Test
    void activateAccount_FailedActivation() throws Exception {
        String token = "invalidToken";

        // Mock the service method to return false for failed activation
        when(authenticationService.activateAccount(token)).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/activate-account")
                        .param("verToken", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to activate account. Please check your token or request a new one."));
    }

    @Test
    void activateAccount_EmailValidationException() throws Exception {
        String token = "invalidToken";

        // Mock the service method to throw EmailValidationException
        when(authenticationService.activateAccount(token)).thenThrow(new EmailValidationException("Invalid token"));

        mockMvc.perform(get("/api/v1/auth/activate-account")
                        .param("verToken", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void activateAccount_RuntimeException() throws Exception {
        String token = "anyToken";

        // Mock the service method to throw a RuntimeException
        when(authenticationService.activateAccount(token)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/auth/activate-account")
                        .param("verToken", token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to activate account"));
    }

    @Test
    void register_SuccessfulRegistration() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("Amine")
                .lastname("Zirek")
                .email("aminezirek@test.com")
                .password("password_test")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder().accessToken("testToken").build();

        when(authenticationService.userExists(request.getEmail())).thenReturn(false);
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"Amine\",\"lastname\":\"Zirek\", \"email\":\"aminezirek@test.com\", \"password\":\"password_test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("testToken"));
    }

    @Test
    void register_UserExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("Amine")
                .lastname("Zirek")
                .email("aminezirek@test.com")
                .password("password_test")
                .build();

        when(authenticationService.userExists(request.getEmail())).thenReturn(true);
        when(authenticationService.register(request)).thenReturn(new AuthenticationResponse());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"Amine\",\"lastname\":\"Zirek\", \"email\":\"aminezirek@test.com\", \"password\":\"password_test\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticate_SuccessfulAuthentication() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("email@test.com", "password");
        AuthenticationResponse response = AuthenticationResponse.builder().accessToken("token").build();

        // Mock the authentication process
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"email@test.com\", \"password\": \"password\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("token"));
    }



    @Test
    void resendVerification_Successful() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest();

        doNothing().when(authenticationService).resendEmailVerification(anyString());

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"email@test.com\" }"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification email resent successfully."));
    }

    @Test
    void checkSession_ValidToken_USER() throws Exception {
        String token = "validToken";
        when(jwtService.getUserRoleFromToken(anyString())).thenReturn(Role.USER);
        when(jwtService.isTokenValid(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/check-session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("User session valid"));
    }

    @Test
    void checkSession_ValidToken_ADMIN() throws Exception {
        String token = "validToken";
        when(jwtService.getUserRoleFromToken(anyString())).thenReturn(Role.ADMIN);
        when(jwtService.isTokenValid(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/check-session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin session valid"));
    }

    @Test
    void checkSession_InvalidToken() throws Exception {
        String token = "invalidToken";
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/check-session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("false"));
    }

}