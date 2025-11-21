package com.example.starter.rbac.user.service;

import com.example.starter.common.exception.ApiException;
import com.example.starter.rbac.mapping.entity.UserRole;
import com.example.starter.rbac.mapping.repository.UserRoleRepository;
import com.example.starter.rbac.role.entity.Role;
import com.example.starter.rbac.role.repository.RoleRepository;
import com.example.starter.rbac.user.dto.UserRequest;
import com.example.starter.rbac.user.dto.UserResponse;
import com.example.starter.rbac.user.entity.UserAccount;
import com.example.starter.rbac.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createAdmin(UserRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        userAccountRepository.save(user);

        Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role ADMIN not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(adminRole);
        userRoleRepository.save(userRole);

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userAccountRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    private UserResponse toResponse(UserAccount user) {
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        Set<String> roleCodes = userRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .roles(roleCodes)
                .build();
    }

    @Transactional
    public void hardDelete(UUID id) {
        if (!userAccountRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        userAccountRepository.hardDeleteById(id);
    }

}
