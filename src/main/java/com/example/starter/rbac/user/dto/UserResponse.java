package com.example.starter.rbac.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean locked;
    private Set<String> roles;
}
