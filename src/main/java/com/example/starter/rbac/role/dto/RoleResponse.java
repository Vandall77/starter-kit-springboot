package com.example.starter.rbac.role.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoleResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
}
