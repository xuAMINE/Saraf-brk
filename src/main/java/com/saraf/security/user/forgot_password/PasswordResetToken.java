package com.saraf.security.user.forgot_password;

import com.saraf.security.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue
    private Integer id;
    private String token;
    private LocalDateTime expirationTime;

    @ManyToOne
    @JoinColumn(name= "user_id", nullable = false)
    private User user;
}
