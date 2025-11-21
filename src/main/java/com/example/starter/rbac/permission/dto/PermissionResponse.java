package com.example.starter.rbac.permission.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PermissionResponse {

    private UUID id;
    private String code;
    private String description;
}
