package com.example.starter.auth.service;

import com.example.starter.auth.dto.LoginRequest;
import com.example.starter.auth.dto.LoginResponse;
import com.example.starter.auth.dto.MeResponse;
import com.example.starter.auth.dto.RefreshTokenRequest;
import com.example.starter.auth.entity.RefreshToken;
import com.example.starter.auth.repository.RefreshTokenRepository;
import com.example.starter.common.exception.ApiException;
import com.example.starter.rbac.mapping.entity.RolePermission;
import com.example.starter.rbac.mapping.entity.UserRole;
import com.example.starter.rbac.mapping.repository.RolePermissionRepository;
import com.example.starter.rbac.mapping.repository.UserRoleRepository;
import com.example.starter.rbac.role.entity.Role;
import com.example.starter.security.JwtTokenProvider;
import com.example.starter.rbac.user.entity.UserAccount;
import com.example.starter.rbac.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateToken(authentication);

        UserAccount user = userAccountRepository.findByUsernameAndDeletedAtIsNull(request.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public MeResponse me(String username) {
        UserAccount user = userAccountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // roles
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        Set<Role> roles = userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());

        // permissions
        List<RolePermission> rolePermissionList = rolePermissionRepository.findByRoleIn(roles);

        Set<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        Set<String> permissionCodes = rolePermissionList.stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());

        return MeResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .build();
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        UserAccount user = refreshToken.getUser();
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, null);
        String newAccessToken = jwtTokenProvider.generateToken(authToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }
}
