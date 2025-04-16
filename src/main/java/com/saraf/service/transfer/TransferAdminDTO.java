package com.saraf.service.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TransferAdminDTO {
    private Integer id;
    private BigDecimal amount;
    private BigDecimal amountReceived;
    private Status status;
    private LocalDateTime transferDate;
    private String recipientFullName;
    private String recipientCCP;
    private String receipt;
    private PaymentMethod paymentMethod;
    private String code;
    private String firstName;
    private String lastName;
}
