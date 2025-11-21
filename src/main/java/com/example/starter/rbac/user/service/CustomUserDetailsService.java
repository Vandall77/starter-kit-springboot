package com.example.starter.rbac.user.service;

import com.example.starter.rbac.mapping.entity.RolePermission;
import com.example.starter.rbac.mapping.entity.UserRole;
import com.example.starter.rbac.mapping.repository.RolePermissionRepository;
import com.example.starter.rbac.mapping.repository.UserRoleRepository;
import com.example.starter.rbac.role.entity.Role;
import com.example.starter.rbac.user.entity.UserAccount;
import com.example.starter.rbac.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ambil user
        UserAccount user = userAccountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Ambil role milik user
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        Set<Role> roles = userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());

        // Ambil mapping role → permission
        List<RolePermission> rolePermissionList = rolePermissionRepository.findByRoleIn(roles);

        // Kumpulkan code permission (akses Permission.getCode() masih dalam transaksi → aman dari LazyInitialization)
        Set<String> permissionCodes = rolePermissionList.stream()
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .collect(Collectors.toSet());

        // Build authorities: ROLE_xxx + permission code
        Set<GrantedAuthority> authorities = new HashSet<>();

        // ROLE_*
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        }

        // Permission code langsung
        for (String code : permissionCodes) {
            authorities.add(new SimpleGrantedAuthority(code));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(user.isLocked())
                .disabled(!user.isEnabled())
                .build();
    }
}
