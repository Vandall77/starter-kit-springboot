package com.example.starter.rbac.user.controller;

import com.example.starter.audit.aspect.Auditable;
import com.example.starter.common.web.ApiResponse;
import com.example.starter.rbac.user.dto.UserRequest;
import com.example.starter.rbac.user.dto.UserResponse;
import com.example.starter.rbac.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/admin")
    @Auditable(action = "USER_CREATE_ADMIN")
    public ApiResponse<UserResponse> createAdmin(@RequestBody @Valid UserRequest request) {
        return ApiResponse.ok(userService.createAdmin(request));
    }

    @PreAuthorize("hasAuthority('USER_HARD_DELETE')")
    @DeleteMapping("/api/users/{id}/hard")
    public ApiResponse<Void> hardDeleteUser(@PathVariable UUID id) {
        userService.hardDelete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll() {
        return ApiResponse.ok(userService.findAll());
    }
}
