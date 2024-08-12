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

    @NotEmpty(message = "Please provide first and last Name.")
    @Size(min = 3, message = "Name should be at least 3 characters long.")
    private String firstName;
    @NotEmpty(message = "Please provide first and last Name.")
    @Size(min = 3, message = "Name should be at least 3 characters long.")
    private String lastName;
    @NotEmpty(message = "CCP cannot be left empty.")
    @Size(min = 11, message = "Please Enter a valid CCP number.")
    private String ccp;
    private String phoneNumber;
    private boolean doContact;
    private User user;
}

