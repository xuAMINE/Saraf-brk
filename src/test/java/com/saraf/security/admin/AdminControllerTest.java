package com.saraf.security.admin;

import com.saraf.security.admin.s3.S3Service;
import com.saraf.security.exception.TransferNotFoundException;
import com.saraf.security.exception.UserNotFoundException;
import com.saraf.security.user.*;
import com.saraf.service.transfer.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransfers_Success() throws Exception {
        Page<TransferDTO> transfers = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(transferService.getTransfersForAdmin(anyInt(), anyInt())).thenReturn(transfers);

        mockMvc.perform(get("/api/v1/admin/transfers")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadReceipt_Success() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setReceipt("receiptUrl");
        MockMultipartFile receipt = new MockMultipartFile("receipt", "receipt.jpg", MediaType.IMAGE_JPEG_VALUE, "receipt content".getBytes());

        when(s3Service.saveReceipt(anyInt(), any())).thenReturn(transfer);

        mockMvc.perform(multipart("/api/v1/admin/upload-receipt/1")
                        .file(receipt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Receipt uploaded successfully"))
                .andExpect(jsonPath("$.data").value("receiptUrl"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadReceipt_TransferNotFound() throws Exception {
        MockMultipartFile receipt = new MockMultipartFile("receipt", "receipt.jpg", MediaType.IMAGE_JPEG_VALUE, "receipt content".getBytes());

        when(s3Service.saveReceipt(anyInt(), any())).thenThrow(new TransferNotFoundException("Transfer not found"));

        mockMvc.perform(multipart("/api/v1/admin/upload-receipt/1")
                        .file(receipt))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Transfer not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadReceipt_IOException() throws Exception {
        MockMultipartFile receipt = new MockMultipartFile("receipt", "receipt.jpg", MediaType.IMAGE_JPEG_VALUE, "receipt content".getBytes());

        when(s3Service.saveReceipt(anyInt(), any())).thenThrow(new IOException("Error uploading receipt"));

        mockMvc.perform(multipart("/api/v1/admin/upload-receipt/1")
                        .file(receipt))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error uploading receipt"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_Success() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setStatus(Status.RECEIVED);

        UpdateTransferStatusDTO updateRequest = new UpdateTransferStatusDTO();
        updateRequest.setId(1);
        updateRequest.setStatus(Status.RECEIVED);

        when(transferService.updateStatus(anyInt(), any())).thenReturn(transfer);

        mockMvc.perform(patch("/api/v1/admin/update-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"id\": 1, \"status\": \"RECEIVED\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"admin:update"})
    void putRole_Success() throws Exception {
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setRole(Role.ADMIN);

        mockMvc.perform(put("/api/v1/admin/user/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"ADMIN\" }"))
                .andExpect(status().isOk())
                .andExpect(content().string("User role updated successfully"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"admin:update"})
    void putRole_UserNotFound() throws Exception {
        // Given
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setRole(Role.ADMIN);

        // Simulate UserNotFoundException being thrown by the service
        doThrow(new UserNotFoundException("User with ID 1 not found"))
                .when(roleService).updateUserRole(1, Role.ADMIN);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/user/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"role\": \"ADMIN\" }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID 1 not found"));
    }



}
