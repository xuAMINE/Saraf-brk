package com.saraf.service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saraf.security.email.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    private ContactRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ContactRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("johndoe@example.com");
        validRequest.setMessage("Hello, I have a question.");
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddContact_Success() throws Exception {
        // Given valid request, mocking the emailService to not throw exceptions
        doNothing().when(emailService).sendContactUsEmail(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendContactConfirmEmail(anyString(), anyString(), anyString());

        // Perform the POST request with the valid contact request
        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Thank you for contacting us, John Doe!"));

        // Verify both emails were sent
        verify(emailService).sendContactUsEmail(validRequest.getName(), validRequest.getEmail(), validRequest.getMessage());
        verify(emailService).sendContactConfirmEmail(validRequest.getName(), validRequest.getEmail(), validRequest.getMessage());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddContact_InvalidRequest() throws Exception {
        // Create an invalid request (missing required fields like name)
        ContactRequest invalidRequest = new ContactRequest();
        invalidRequest.setEmail("johndoe@example.com");
        invalidRequest.setMessage("Hello, I have a question.");

        // Perform the POST request with the invalid request
        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddContact_EmailSendingFailure() throws Exception {
        // Mock the emailService to throw a MessagingException
        doThrow(new MessagingException("Email sending failed")).when(emailService).sendContactUsEmail(anyString(), anyString(), anyString());

        // Perform the POST request with the valid contact request
        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MessagingException))
                .andExpect(result -> assertEquals("Email sending failed", result.getResolvedException().getMessage()));
    }

    // Utility method to convert object to JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}