package com.example.starter.rbac.role.service;

import com.example.starter.common.exception.ApiException;
import com.example.starter.rbac.role.dto.RoleRequest;
import com.example.starter.rbac.role.dto.RoleResponse;
import com.example.starter.rbac.role.entity.Role;
import com.example.starter.rbac.role.repository.RoleRepository;
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
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        return toResponse(role);
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        // asumsi: RoleRepository.findByCode(String) -> Optional<Role>
        Optional<Role> existing = roleRepository.findByCode(request.getCode());
        if (existing.isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role code already exists");
        }

        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        roleRepository.save(role);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse update(UUID id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));

        // kalau code mau diganti, cek dulu unik
        if (!role.getCode().equals(request.getCode())) {
            Optional<Role> other = roleRepository.findByCode(request.getCode());
            if (other.isPresent()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Role code already exists");
            }
        }

        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        roleRepository.save(role);
        return toResponse(role);
    }

    /**
     * SOFT DELETE – pakai @SQLDelete di Role entity
     */
    @Transactional
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        roleRepository.delete(role);
    }

    /**
     * HARD DELETE – benar-benar DELETE FROM roles
     */
    @Transactional
    public void hardDelete(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Role not found");
        }
        roleRepository.hardDeleteById(id);
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }

}
