package com.example.starter.auth.controller;

import com.example.starter.auth.dto.LoginRequest;
import com.example.starter.auth.dto.LoginResponse;
import com.example.starter.auth.dto.MeResponse;
import com.example.starter.auth.dto.RefreshTokenRequest;
import com.example.starter.auth.service.AuthService;
import com.example.starter.audit.aspect.Auditable;
import com.example.starter.common.exception.ApiException;
import com.example.starter.common.web.ApiResponse;
import com.example.starter.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Auditable(action = "LOGIN")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String username = jwtTokenProvider.getUsername(token);

        return ApiResponse.ok(authService.me(username));
    }
}
