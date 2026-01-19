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
import com.nines.nutsfact.api.v1.request.auth.OAuthWithInvitationCodeRequest;
import com.nines.nutsfact.api.v1.request.auth.PasswordResetRequest;
import com.nines.nutsfact.api.v1.request.auth.PasswordUpdateRequest;
import com.nines.nutsfact.api.v1.request.auth.RefreshTokenRequest;
import com.nines.nutsfact.api.v1.request.auth.SignUpWithInvitationCodeRequest;
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

    @PostMapping("/signUpWithInvitationCode")
    public ResponseEntity<Map<String, Object>> signUpWithInvitationCode(
            @Valid @RequestBody SignUpWithInvitationCodeRequest request) {
        AuthService.AuthResult result = authService.signUpWithInvitationCode(
                request.getEmail(), request.getPassword(), request.getName(), request.getInvitationCode());
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

    /**
     * OAuth + 招待コードでの新規登録
     */
    @PostMapping("/signInWithOAuthAndInvitationCode")
    public ResponseEntity<Map<String, Object>> signInWithOAuthAndInvitationCode(
            @Valid @RequestBody OAuthWithInvitationCodeRequest request) {
        AuthService.AuthResult result = authService.signInWithOAuthAndInvitationCode(
                request.getProvider(),
                request.getIdToken(),
                request.getAccessToken(),
                request.getInvitationCode());
        return ResponseEntity.ok(buildAuthResponse(result));
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

    /**
     * ユーザーの所属ビジネスアカウント一覧を取得
     */
    @GetMapping("/memberships")
    public ResponseEntity<Map<String, Object>> getMemberships(Authentication authentication) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();
        AuthService.MembershipsResult result = authService.getUserMemberships(authUser.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("userId", result.userId());
        response.put("memberships", result.memberships());
        return ResponseEntity.ok(response);
    }

    /**
     * ビジネスアカウントを選択（ログイン後のアカウント選択・アカウント切り替え）
     */
    @PostMapping("/selectAccount")
    public ResponseEntity<Map<String, Object>> selectAccount(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();
        // クライアントはsnake_case (business_account_id) で送信する
        Object businessAccountIdObj = request.get("business_account_id");
        if (businessAccountIdObj == null) {
            // camelCase (businessAccountId) もサポート（後方互換性）
            businessAccountIdObj = request.get("businessAccountId");
        }
        if (businessAccountIdObj == null) {
            throw new IllegalArgumentException("business_account_idは必須です");
        }
        Integer businessAccountId = ((Number) businessAccountIdObj).intValue();
        AuthService.AuthResult result = authService.selectBusinessAccount(authUser.getUserId(), businessAccountId);
        return ResponseEntity.ok(buildAuthResponse(result));
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
        // マルチアカウント対応フィールド
        if (result.memberships() != null && !result.memberships().isEmpty()) {
            response.put("memberships", result.memberships());
        }
        response.put("needsAccountSelection", result.needsAccountSelection());
        return response;
    }
}
