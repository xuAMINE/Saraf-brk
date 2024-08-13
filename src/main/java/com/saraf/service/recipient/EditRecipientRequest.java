package com.saraf.service.recipient;

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
public class EditRecipientRequest {

        @NotEmpty(message = "Please provide first and last Name.")
        @Size(min = 3, message = "Name should be at least 3 characters long.")
        private String firstName;
        @NotEmpty(message = "Please provide first and last Name.")
        @Size(min = 3, message = "Name should be at least 3 characters long.")
        private String lastName;
        private String phoneNumber;
        private boolean doContact;

}