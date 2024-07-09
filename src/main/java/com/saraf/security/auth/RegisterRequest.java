package com.saraf.security.auth;

import com.saraf.security.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class RegisterRequest {

  @NotEmpty(message = "first name should not be empty")
  @NotBlank(message = "first name should not be empty")
  private String firstname;
  @NotEmpty(message = "last name should not be empty")
  @NotBlank(message = "last name should not be empty")
  private String lastname;
  @NotEmpty(message = "email should not be empty")
  @NotBlank(message = "email should not be empty")
  @Email(message = "Please enter a correct email!")
  private String email;
  @NotEmpty(message = "password can not be empty")
  @NotBlank(message = "password can not be empty")
  @Size(min = 8, message = "Password should be at least 8 characters long")
  private String password;
  private Role role;
}
