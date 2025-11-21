package com.example.starter.rbac.permission.controller;

import com.example.starter.common.web.ApiResponse;
import com.example.starter.rbac.permission.dto.PermissionRequest;
import com.example.starter.rbac.permission.dto.PermissionResponse;
import com.example.starter.rbac.permission.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ApiResponse<PermissionResponse> getById(@PathVariable("id") UUID id) {
        return ApiResponse.ok(permissionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ApiResponse<PermissionResponse> create(@RequestBody @Valid PermissionRequest request) {
        return ApiResponse.ok(permissionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ApiResponse<PermissionResponse> update(
            @PathVariable("id") UUID id,
            @RequestBody @Valid PermissionRequest request
    ) {
        return ApiResponse.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ApiResponse<Void> delete(@PathVariable("id") UUID id) {
        permissionService.delete(id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasAuthority('PERMISSION_HARD_DELETE')")
    public ApiResponse<Void> hardDelete(@PathVariable("id") UUID id) {
        permissionService.hardDelete(id);
        return ApiResponse.ok(null);
    }
}
