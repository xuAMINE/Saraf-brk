package com.saraf.service.transfer;

import com.saraf.security.admin.s3.S3Service;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
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

    @Test
    @WithMockUser(roles = "USER")
    void addTransfer() throws Exception {
        Transfer transfer = Transfer.builder().id(1).amount(BigDecimal.valueOf(100)).build();
        Mockito.when(transferService.addTransfer(Mockito.any(TransferRequest.class))).thenReturn(transfer);

        mockMvc.perform(post("/api/v1/transfer/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100,\"ccp\":\"123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(100)));
    }

//    @Test
//    @WithMockUser(roles = "USER")
//    void getTransfersForUser() throws Exception {
//        TransferDTO transferDTO = new TransferDTO(1, BigDecimal.valueOf(100), BigDecimal.valueOf(90), Status.PENDING, LocalDateTime.now(), "Jane Doe", "receipt1");
//        Mockito.when(transferService.getTransfersForUser()).thenReturn(List.of(transferDTO));
//
//        mockMvc.perform(get("/api/v1/transfer"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id", is(1)))
//                .andExpect(jsonPath("$[0].amount", is(100)))
//                .andExpect(jsonPath("$[0].recipientFullName", is("Jane Doe")));
//    }

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
