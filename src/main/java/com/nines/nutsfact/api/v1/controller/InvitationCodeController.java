package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.InvitationCodeCreateRequest;
import com.nines.nutsfact.api.v1.request.InvitationCodeUseRequest;
import com.nines.nutsfact.api.v1.request.InvitationCodeVerifyRequest;
import com.nines.nutsfact.config.AuthenticatedUser;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.InvitationCode;
import com.nines.nutsfact.domain.service.AuthService;
import com.nines.nutsfact.domain.service.BusinessAccountService;
import com.nines.nutsfact.domain.service.InvitationCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/InvitationCode")
@RequiredArgsConstructor
public class InvitationCodeController {

    private final InvitationCodeService invitationCodeService;
    private final BusinessAccountService businessAccountService;
    private final AuthService authService;

    @GetMapping("/GetData")
    public ResponseEntity<Map<String, Object>> getData(Authentication authentication) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        // businessAccountIdがnullの場合は空リストを返す
        List<InvitationCode> codes;
        if (authUser.getBusinessAccountId() == null) {
            codes = List.of();
        } else {
            codes = invitationCodeService.findByBusinessAccountId(
                    authUser.getBusinessAccountId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", codes.size());
        response.put("item", codes);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            Authentication authentication,
            @Valid @RequestBody InvitationCodeCreateRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        // businessAccountIdがnullの場合はエラー
        if (authUser.getBusinessAccountId() == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Error");
            response.put("message", "ビジネスアカウントに所属していないため、招待を作成できません");
            return ResponseEntity.badRequest().body(response);
        }

        InvitationCode created = invitationCodeService.create(
                authUser.getBusinessAccountId(),
                request.getEmail(),
                request.getRole(),
                request.getExpirationDays(),
                authUser.getUserId());

        return ResponseEntity.ok(buildInvitationCodeResponse(created));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@Valid @RequestBody InvitationCodeVerifyRequest request) {
        InvitationCodeService.InvitationCodeVerifyResult result =
                invitationCodeService.verify(request.getCode(), request.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("isValid", result.isValid());

        if (result.isValid()) {
            InvitationCode code = result.getInvitationCode();
            BusinessAccount businessAccount = businessAccountService.findById(code.getBusinessAccountId());

            response.put("businessAccountName", businessAccount.getCompanyName());
            response.put("role", code.getRole());
            response.put("expiresAt", code.getExpiresAt());
        } else {
            response.put("errorCode", result.getErrorCode());
            response.put("errorMessage", result.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/use")
    public ResponseEntity<Map<String, Object>> use(
            Authentication authentication,
            @Valid @RequestBody InvitationCodeUseRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        AuthService.AuthResult result = authService.useInvitationCode(authUser.getUserId(), request.getCode());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("user", result.user());
        response.put("businessAccount", result.businessAccount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revoke(@RequestParam("id") Integer id) {
        invitationCodeService.revoke(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend")
    public ResponseEntity<Map<String, Object>> resend(@RequestParam("id") Integer id) {
        invitationCodeService.findById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("message", "招待メールを再送信しました");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildInvitationCodeResponse(InvitationCode code) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", code.getId());
        response.put("businessAccountId", code.getBusinessAccountId());
        response.put("code", code.getCode());
        response.put("email", code.getEmail());
        response.put("role", code.getRole());
        response.put("expiresAt", code.getExpiresAt());
        response.put("isUsed", code.getIsUsed());
        response.put("usedAt", code.getUsedAt());
        response.put("createDate", code.getCreateDate());
        response.put("createdByUserId", code.getCreatedByUserId());
        return response;
    }
}
