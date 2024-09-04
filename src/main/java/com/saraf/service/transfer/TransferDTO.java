package com.saraf.service.transfer;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferDTO {
    private Integer id;
    private BigDecimal amount;
    private BigDecimal amountReceived;
    private Status status;
    private LocalDateTime transferDate;
    private String recipientFullName;
    private String receipt;

    public TransferDTO(Integer id, BigDecimal amount, BigDecimal amountReceived, Status status, LocalDateTime transferDate, String recipientFullName, String receipt) {
        this.id = id;
        this.amount = amount;
        this.amountReceived = amountReceived;
        this.status = status;
        this.transferDate = transferDate;
        this.recipientFullName = recipientFullName;
        this.receipt = receipt;
    }
}

