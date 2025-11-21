package com.example.starter.rbac.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    private String username;

    private String email;

    @NotBlank
    private String password;
}
