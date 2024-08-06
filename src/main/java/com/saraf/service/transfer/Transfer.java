package com.saraf.service.transfer;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Transfer {

    @Id
    @GeneratedValue
    private Integer id;
    private BigDecimal amount;
    private BigDecimal amountReceived;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate transferDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @JsonManagedReference
    private Recipient recipient;

}
