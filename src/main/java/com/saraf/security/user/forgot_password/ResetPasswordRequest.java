package com.saraf.security.user.forgot_password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResetPasswordRequest {

    @NotEmpty(message = "password can not be empty")
    @NotBlank(message = "password can not be empty")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    private String newPassword;
    private String confirmPassword;
}
