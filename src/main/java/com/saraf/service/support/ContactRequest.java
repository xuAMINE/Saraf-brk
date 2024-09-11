package com.saraf.service.support;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequest {

    @NotEmpty(message = "Please provide your Name.")
    String name;
    @NotEmpty(message = "Please provide your email.")
    String email;
    @NotEmpty(message = "message can not be empty.")
    @Size(min = 15, message = "message is too short")
    String message;
}
