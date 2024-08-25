package com.saraf.service.recipient;

import com.saraf.security.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RecipientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipientService recipientService;

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRecipients() throws Exception {
        Recipient recipient = Recipient.builder().firstname("John").lastname("Doe").ccp("123").build();
        Mockito.when(recipientService.getAllRecipients()).thenReturn(List.of(recipient));

        mockMvc.perform(get("/api/v1/recipient"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'firstname':'John', 'lastname':'Doe', 'ccp':'123'}]"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAddRecipient() throws Exception {
        Recipient recipient = Recipient.builder().firstname("John").lastname("Doe").ccp("123456789089").build();
        Mockito.when(recipientService.addRecipient(Mockito.any())).thenReturn(recipient);

        mockMvc.perform(post("/api/v1/recipient/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"ccp\":\"123456789089\",\"phoneNumber\":\"664440342\",\"doContact\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'firstname':'John', 'lastname':'Doe', 'ccp':'123456789089'}"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testEditRecipient() throws Exception {
        EditRecipientRequest request = new EditRecipientRequest("John", "Doe", "1234567890", true);
        Recipient recipient = Recipient.builder().firstname("John").lastname("Doe").ccp("123").build();
        Mockito.when(recipientService.editRecipient(Mockito.eq("123"), Mockito.any())).thenReturn(recipient);

        mockMvc.perform(put("/api/v1/recipient/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phoneNumber\":\"1234567890\",\"doContact\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'firstname':'John', 'lastname':'Doe', 'ccp':'123'}"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeactivateRecipientByCcp() throws Exception {
        mockMvc.perform(put("/api/v1/recipient/deactivate/123"))
                .andExpect(status().isNoContent());

        Mockito.verify(recipientService).deactivateRecipient("123");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetRecipientsForCurrentUser() throws Exception {
        Recipient recipient = Recipient.builder().firstname("John").lastname("Doe").ccp("123").build();
        Mockito.when(recipientService.getRecipientsForCurrentUser()).thenReturn(List.of(recipient));

        mockMvc.perform(get("/api/v1/recipient/current-user"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'firstname':'John', 'lastname':'Doe', 'ccp':'123'}]"));
    }
}
