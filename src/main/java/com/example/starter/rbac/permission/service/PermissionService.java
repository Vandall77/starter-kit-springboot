package com.example.starter.rbac.permission.service;

import com.example.starter.common.exception.ApiException;
import com.example.starter.rbac.permission.dto.PermissionRequest;
import com.example.starter.rbac.permission.dto.PermissionResponse;
import com.example.starter.rbac.permission.entity.Permission;
import com.example.starter.rbac.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll() {
        List<Permission> list = permissionRepository.findAll();
        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(UUID id) {
        Permission perm = permissionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Permission not found"));
        return toResponse(perm);
    }

    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        Optional<Permission> existing = permissionRepository.findByCode(request.getCode());
        if (existing.isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Permission code already exists");
        }

        Permission perm = new Permission();
        perm.setCode(request.getCode());
        perm.setDescription(request.getDescription());

        permissionRepository.save(perm);
        return toResponse(perm);
    }

    @Transactional
    public PermissionResponse update(UUID id, PermissionRequest request) {
        Permission perm = permissionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Permission not found"));

        if (!perm.getCode().equals(request.getCode())) {
            Optional<Permission> other = permissionRepository.findByCode(request.getCode());
            if (other.isPresent()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Permission code already exists");
            }
        }

        perm.setCode(request.getCode());
        perm.setDescription(request.getDescription());

        permissionRepository.save(perm);
        return toResponse(perm);
    }

    /**
     * SOFT DELETE – pakai @SQLDelete di Permission entity
     */
    @Transactional
    public void delete(UUID id) {
        Permission perm = permissionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Permission not found"));
        permissionRepository.delete(perm);
    }

    /**
     * HARD DELETE – benar-benar DELETE FROM permissions
     */
    @Transactional
    public void hardDelete(UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Permission not found");
        }
        permissionRepository.hardDeleteById(id);
    }

    private PermissionResponse toResponse(Permission perm) {
        return PermissionResponse.builder()
                .id(perm.getId())
                .code(perm.getCode())
                .description(perm.getDescription())
                .build();
    }
}
