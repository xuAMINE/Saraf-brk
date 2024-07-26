package com.saraf.service.recipient;

import com.saraf.security.user.User;
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
public class RecipientRequest {

    @NotEmpty(message = "first name should not be empty")
    @Size(min = 3, message = "first name should be at least 3 characters long")
    private String firstName;
    @NotEmpty(message = "last name should not be empty")
    @Size(min = 3, message = "last name should be at least 3 characters long")
    private String lastName;
    @NotEmpty
    private String ccp;
    private String phoneNumber;
    private boolean doContact;
    private User user;
}

