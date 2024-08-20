package com.saraf.service.transfer;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransferDTO {
    private Integer id;
    private BigDecimal amount;
    private BigDecimal amountReceived;
    private Status status;
    private LocalDate transferDate;
    private String recipientFullName;
    private String receipt;

    public TransferDTO(Integer id, BigDecimal amount, BigDecimal amountReceived, Status status, LocalDate transferDate, String recipientFullName, String receipt) {
        this.id = id;
        this.amount = amount;
        this.amountReceived = amountReceived;
        this.status = status;
        this.transferDate = transferDate;
        this.recipientFullName = recipientFullName;
        this.receipt = receipt;
    }
}

