package com.example.spring.booking.web.model.user;

import com.example.spring.booking.entity.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;;
    private Role role;
}
