package com.saraf.security.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saraf.security.user.forgot_password.ForgotPasswordRequest;
import com.saraf.security.user.forgot_password.ResetPasswordRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void testChangePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("current_password", "new_password", "new_password");

        mockMvc.perform(patch("/api/v1/user")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testChangePassword_Failure_IncorrectCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong_password", "new_password", "new_password");
        Mockito.doThrow(new IllegalStateException("Incorrect password"))
                .when(userService).changePassword(Mockito.any(ChangePasswordRequest.class), Mockito.any());

        mockMvc.perform(patch("/api/v1/user")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("johndoe@example.com");

        mockMvc.perform(post("/api/v1/user/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testForgotPassword_Failure_UserNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
        Mockito.doThrow(new IllegalStateException("User not found"))
                .when(userService).forgotPassword(Mockito.any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/user/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .newPassword("new_password")
                .confirmPassword("new_password")
                .build();

        mockMvc.perform(post("/api/v1/user/reset-password")
                        .param("token", "validToken")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testResetPassword_Failure_TokenExpired() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .newPassword("new_password")
                .confirmPassword("new_password")
                .build();

        Mockito.doThrow(new IllegalStateException("Session is expired, Please make a new request."))
                .when(userService).resetPassword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        mockMvc.perform(post("/api/v1/user/reset-password")
                        .param("token", "expiredToken")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
