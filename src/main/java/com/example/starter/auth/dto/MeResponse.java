package com.example.starter.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class MeResponse {

    private String username;
    private String email;
    private Set<String> roles;
    private Set<String> permissions;
}
