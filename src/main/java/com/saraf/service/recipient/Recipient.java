package com.saraf.service.recipient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saraf.security.user.User;
import com.saraf.service.transfer.Transfer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipient",
        uniqueConstraints = @UniqueConstraint(name = "unique_recipient_per_user", columnNames = {"user_id", "ccp"}))
public class Recipient {

    @Id
    @GeneratedValue
    private Integer id;
    private String firstname;
    private String lastname;
    private String ccp;
    private String phoneNumber;
    private boolean doContact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transfer> transfers;
}
