package com.saraf.security.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class ResendVerificationRequest {
    private String email;
}
