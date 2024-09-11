package com.saraf.service.transfer;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.saraf.security.user.User;
import com.saraf.service.recipient.Recipient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "transfer_generator")
    @TableGenerator(name = "transfer_generator", initialValue = 1001)
    private Integer id;
    private BigDecimal amount;
    private BigDecimal amountReceived;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime transferDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @JsonIgnore
    private Recipient recipient;

}
