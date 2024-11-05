package com.saraf.service.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saraf.security.admin.s3.S3Service;
import com.saraf.security.exception.TransferNotPendingException;
import com.saraf.service.telegram.TelegramBot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.postgresql.hostchooser.HostRequirement.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private TelegramBot telegramBot;

    @Test
    @WithMockUser(roles = "USER")
    void testAddTransfer_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        // Set valid request data
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod(PaymentMethod.ZELLE);
        request.setCode("ABC1");
        request.setCcp("12345678900");

        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setAmount(request.getAmount());

        TransferAdminDTO telegramTransfer = transferService.getTransferById(transfer.getId());
        telegramBot.sendTransferToChannel(telegramTransfer);

        Mockito.when(transferService.addTransfer(any(TransferRequest.class))).thenReturn(transfer);
        Mockito.when(transferService.getTransferById(transfer.getId())).thenReturn(telegramTransfer);
        Mockito.doNothing().when(telegramBot).sendTransferToChannel(telegramTransfer);

        mockMvc.perform(post("/api/v1/transfer/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.00));

        Mockito.verify(transferService, Mockito.times(2)).getTransferById(transfer.getId());
        Mockito.verify(telegramBot, Mockito.times(2)).sendTransferToChannel(telegramTransfer);

    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddTransfer_ValidationFailure() throws Exception {
        TransferRequest request = new TransferRequest();
        // Invalid request data (missing required fields)

        mockMvc.perform(post("/api/v1/transfer/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransfers_Success() throws Exception {
        TransferDTO transferDTO = TransferDTO.builder()
                .id(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.ZELLE)
                .code("ABC123")
                .build();

        Page<TransferDTO> transfers = new PageImpl<>(List.of(transferDTO));

        Mockito.when(transferService.getTransfersForUser(anyInt(), anyInt())).thenReturn(transfers);

        mockMvc.perform(get("/api/v1/transfer")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(100.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetNonCancelledTransfers_Success() throws Exception {
        TransferDTO transferDTO = TransferDTO.builder()
                .id(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.ZELLE)
                .code("ABC123")
                .build();

        Page<TransferDTO> transfers = new PageImpl<>(List.of(transferDTO));

        Mockito.when(transferService.getNonCancelledTransfersForUser(anyInt(), anyInt())).thenReturn(transfers);

        mockMvc.perform(get("/api/v1/transfer/NonCancelled")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(100.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCheckTransferCredentials_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        // Set valid request data
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod(PaymentMethod.VENMO);
        request.setCode("ABC1");
        request.setCcp("123456789000");

        mockMvc.perform(post("/api/v1/transfer/check-transfer-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCancelTransfer_Success() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setStatus(Status.CANCELED);

        Mockito.when(transferService.cancelTransfer(1)).thenReturn(transfer);

        mockMvc.perform(patch("/api/v1/transfer/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCancelTransfer_TransferNotPending() throws Exception {
        Mockito.when(transferService.cancelTransfer(1)).thenThrow(new TransferNotPendingException("Cannot cancel transfer"));

        mockMvc.perform(patch("/api/v1/transfer/cancel/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Cannot cancel transfer\"}"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserPhone_Success() throws Exception {
        Mockito.when(transferService.getUserPhoneNumberByTransferId(1)).thenReturn("123-456-7890");

        mockMvc.perform(get("/api/v1/transfer/user-phone/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("123-456-7890"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserPhone_NotFound() throws Exception {
        Mockito.when(transferService.getUserPhoneNumberByTransferId(1)).thenReturn(null);

        mockMvc.perform(get("/api/v1/transfer/user-phone/1"))
                .andExpect(status().isNotFound());
    }

    // Utility method to convert object to JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    void getFileUrl() throws Exception {
        Mockito.when(transferService.getReceiptName(1)).thenReturn("receipt1");
        Mockito.when(s3Service.generatePreSignedUrl("receipt1", 10)).thenReturn("https://s3.amazonaws.com/bucket/receipt1");

        mockMvc.perform(get("/api/v1/transfer/receipt/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://s3.amazonaws.com/bucket/receipt1"));
    }
}
