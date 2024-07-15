package com.saraf.security.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VerToken {

    @Id
    @GeneratedValue
    private Integer id;
    private String token;
    private LocalDateTime expires;
    private LocalDateTime created;
    private LocalDateTime validatedAt;

    @ManyToOne
    @JoinColumn(name= "user_id", nullable = false)
    private User user;

    public boolean isActive() {
        return validatedAt == null && expires.isAfter(LocalDateTime.now());
    }

    public void markAsExpired() {
        this.expires = LocalDateTime.now();
    }
}
