package com.saraf.service.transfer;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    @NotNull(message = "Amount can not be left empty")
    @Min(value = 100, message = "Amount must be at least 100")
    private BigDecimal amount;

    @Size(min = 11, message = "Please Enter a valid CCP number")
    private String ccp;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Size(min = 4, max = 4, message = "Code must be of length 4")
    private String code;

}
