package com.example.spring.booking.web.model.user;

import com.example.spring.booking.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Name must be specified!")
    @Size(min = 2, max = 20, message = "Username must contain from 3 to 20 symbols!")
    private String username;

    @NotBlank(message = "Password must be specified!")
    @Size(min = 6, message = "Password must contain at least 6 symbols!")
    private String password;

    @NotBlank(message = "Email must be specified!")
    @Email(message = "Incorrect email!")
    private String email;

    @NotNull(message = "Role must be specified!")
    private Role role;
}
