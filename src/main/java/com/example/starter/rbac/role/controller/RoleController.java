package com.example.starter.rbac.role.controller;

import com.example.starter.common.web.ApiResponse;
import com.example.starter.rbac.role.dto.RoleRequest;
import com.example.starter.rbac.role.dto.RoleResponse;
import com.example.starter.rbac.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleResponse> getById(@PathVariable("id") UUID id) {
        return ApiResponse.ok(roleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<RoleResponse> update(
            @PathVariable("id") UUID id,
            @RequestBody @Valid RoleRequest request
    ) {
        return ApiResponse.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ApiResponse<Void> delete(@PathVariable("id") UUID id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasAuthority('ROLE_HARD_DELETE')")
    public ApiResponse<Void> hardDelete(@PathVariable("id") UUID id) {
        roleService.hardDelete(id);
        return ApiResponse.ok(null);
    }
}
