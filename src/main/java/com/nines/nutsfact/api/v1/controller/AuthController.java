package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.auth.LoginRequest;
import com.nines.nutsfact.api.v1.request.auth.OAuthRequest;
import com.nines.nutsfact.api.v1.request.auth.PasswordResetRequest;
import com.nines.nutsfact.api.v1.request.auth.PasswordUpdateRequest;
import com.nines.nutsfact.api.v1.request.auth.RefreshTokenRequest;
import com.nines.nutsfact.api.v1.request.auth.SignUpRequest;
import com.nines.nutsfact.config.AuthenticatedUser;
import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/Auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signUp")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody SignUpRequest request) {
        AuthService.AuthResult result = authService.signUp(
                request.getEmail(), request.getPassword(), request.getName());
        return ResponseEntity.ok(buildAuthResponse(result));
    }

    @PostMapping("/signIn")
    public ResponseEntity<Map<String, Object>> signIn(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.signIn(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(buildAuthResponse(result));
    }

    @PostMapping("/signInWithOAuth")
    public ResponseEntity<Map<String, Object>> signInWithOAuth(@Valid @RequestBody OAuthRequest request) {
        AuthService.AuthResult result = authService.signInWithOAuth(
                request.getProvider(), request.getIdToken(), request.getAccessToken());
        Map<String, Object> response = buildAuthResponse(result);
        response.put("isNewUser", result.isNewUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signOut")
    public ResponseEntity<Map<String, Object>> signOut() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthToken token = authService.refreshToken(request.getRefreshToken());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requestPasswordReset")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("message", "パスワードリセット用のメールを送信しました");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<Map<String, Object>> updatePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordUpdateRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();
        authService.updatePassword(authUser.getUserId(), request.getNewPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getCurrentUser")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();
        AuthService.CurrentUserResult result = authService.getCurrentUser(authUser.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("authUser", result.authUser());
        response.put("user", result.user());
        if (result.businessAccount() != null) {
            response.put("businessAccount", result.businessAccount());
        }
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildAuthResponse(AuthService.AuthResult result) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("authUser", result.authUser());
        response.put("token", result.token());
        if (result.user() != null) {
            response.put("user", result.user());
        }
        if (result.businessAccount() != null) {
            response.put("businessAccount", result.businessAccount());
        }
        if (result.message() != null) {
            response.put("message", result.message());
        }
        return response;
    }
}
