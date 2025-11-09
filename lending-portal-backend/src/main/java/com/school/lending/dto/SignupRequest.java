package com.school.lending.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50)
    public String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 100)
    public String password;

    public String displayName;
}
