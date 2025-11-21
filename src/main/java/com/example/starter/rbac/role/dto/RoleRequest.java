package com.example.starter.rbac.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;
}
