package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.BusinessAccountCreateRequest;
import com.nines.nutsfact.api.v1.request.BusinessAccountUpdateRequest;
import com.nines.nutsfact.api.v1.request.InvitationCodeCreateRequest;
import com.nines.nutsfact.config.AuthenticatedUser;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.InvitationCode;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.service.BusinessAccountService;
import com.nines.nutsfact.domain.service.InvitationCodeService;
import com.nines.nutsfact.domain.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/BusinessAccount")
@RequiredArgsConstructor
public class BusinessAccountController {

    private final BusinessAccountService businessAccountService;
    private final UserService userService;
    private final InvitationCodeService invitationCodeService;

    /**
     * ビジネスアカウント一覧取得（運営管理者用）
     */
    @GetMapping("/findAll")
    public ResponseEntity<Map<String, Object>> findAll() {
        var accounts = businessAccountService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("items", accounts.stream().map(this::toResponseMap).toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("id") Integer id) {
        BusinessAccount businessAccount = businessAccountService.findById(id);
        return ResponseEntity.ok(buildResponse(businessAccount));
    }

    @GetMapping("/getCurrent")
    public ResponseEntity<Map<String, Object>> getCurrent(Authentication authentication) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        if (!authUser.hasBusinessAccount()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Fail");
            response.put("message", "ビジネスアカウントが設定されていません");
            return ResponseEntity.ok(response);
        }

        BusinessAccount businessAccount = businessAccountService.findById(authUser.getBusinessAccountId());
        return ResponseEntity.ok(buildResponse(businessAccount));
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            Authentication authentication,
            @Valid @RequestBody BusinessAccountCreateRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        BusinessAccount businessAccount = new BusinessAccount();
        businessAccount.setCompanyName(request.getCompanyName());
        businessAccount.setContactPhone(request.getContactPhone());
        businessAccount.setLogoImageUrl(request.getLogoImageUrl());
        businessAccount.setWebsiteUrl(request.getWebsiteUrl());
        businessAccount.setMaxUserCount(request.getMaxUserCount());

        BusinessAccount created = businessAccountService.create(businessAccount);

        User user = userService.findById(authUser.getUserId());
        user.setBusinessAccountId(created.getId());
        user.setRole(1);
        userService.update(authUser.getUserId(), user);

        businessAccountService.updateCurrentUserCount(created.getId());

        return ResponseEntity.ok(buildResponse(created));
    }

    /**
     * ビジネスアカウント作成（運営管理者用）
     * 作成者をビジネスアカウントに紐づけない
     */
    @PostMapping("/insertAdmin")
    public ResponseEntity<Map<String, Object>> insertAdmin(
            @Valid @RequestBody BusinessAccountCreateRequest request) {
        BusinessAccount businessAccount = new BusinessAccount();
        businessAccount.setCompanyName(request.getCompanyName());
        businessAccount.setContactPhone(request.getContactPhone());
        businessAccount.setLogoImageUrl(request.getLogoImageUrl());
        businessAccount.setWebsiteUrl(request.getWebsiteUrl());
        businessAccount.setMaxUserCount(request.getMaxUserCount());

        BusinessAccount created = businessAccountService.create(businessAccount);
        return ResponseEntity.ok(buildResponse(created));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody BusinessAccountUpdateRequest request) {
        BusinessAccount existing = businessAccountService.findById(request.getId());

        if (request.getCompanyName() != null) {
            existing.setCompanyName(request.getCompanyName());
        }
        if (request.getContactPhone() != null) {
            existing.setContactPhone(request.getContactPhone());
        }
        if (request.getLogoImageUrl() != null) {
            existing.setLogoImageUrl(request.getLogoImageUrl());
        }
        if (request.getWebsiteUrl() != null) {
            existing.setWebsiteUrl(request.getWebsiteUrl());
        }
        if (request.getMaxUserCount() != null) {
            existing.setMaxUserCount(request.getMaxUserCount());
        }

        BusinessAccount updated = businessAccountService.update(request.getId(), existing);
        return ResponseEntity.ok(buildResponse(updated));
    }

    @PostMapping("/requestDeletion")
    public ResponseEntity<Map<String, Object>> requestDeletion(@RequestParam("id") Integer id) {
        businessAccountService.requestDeletion(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("message", "削除申請を受け付けました");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suspend")
    public ResponseEntity<Map<String, Object>> suspend(@RequestParam("id") Integer id) {
        businessAccountService.suspend(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reactivate")
    public ResponseEntity<Map<String, Object>> reactivate(@RequestParam("id") Integer id) {
        businessAccountService.reactivate(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * ビジネスオーナー招待（運営管理者用）
     * 指定したビジネスアカウントに対して招待コードを発行
     */
    @PostMapping("/inviteOwner")
    public ResponseEntity<Map<String, Object>> inviteOwner(
            Authentication authentication,
            @RequestParam("businessAccountId") Integer businessAccountId,
            @Valid @RequestBody InvitationCodeCreateRequest request) {
        AuthenticatedUser authUser = (AuthenticatedUser) authentication.getPrincipal();

        // ビジネスアカウントの存在確認
        BusinessAccount businessAccount = businessAccountService.findById(businessAccountId);

        // ビジネスオーナーとして招待（role = 10）
        Integer role = request.getRole() != null ? request.getRole() : 10;

        InvitationCode created = invitationCodeService.create(
                businessAccountId,
                request.getEmail(),
                role,
                request.getExpirationDays(),
                authUser.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", buildInvitationCodeMap(created));
        response.put("businessAccountName", businessAccount.getCompanyName());
        return ResponseEntity.ok(response);
    }

    /**
     * 招待コード一覧取得（運営管理者用）
     */
    @GetMapping("/getInvitations")
    public ResponseEntity<Map<String, Object>> getInvitations(
            @RequestParam("businessAccountId") Integer businessAccountId) {
        var codes = invitationCodeService.findByBusinessAccountId(businessAccountId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("items", codes.stream().map(this::buildInvitationCodeMap).toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 招待コード取り消し（運営管理者用）
     */
    @PostMapping("/revokeInvitation")
    public ResponseEntity<Map<String, Object>> revokeInvitation(@RequestParam("id") Integer id) {
        invitationCodeService.revoke(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildInvitationCodeMap(InvitationCode code) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", code.getId());
        map.put("businessAccountId", code.getBusinessAccountId());
        map.put("code", code.getCode());
        map.put("email", code.getEmail());
        map.put("role", code.getRole());
        map.put("expiresAt", code.getExpiresAt());
        map.put("isUsed", code.getIsUsed());
        map.put("usedAt", code.getUsedAt());
        map.put("createDate", code.getCreateDate());
        map.put("createdByUserId", code.getCreatedByUserId());
        return map;
    }

    private Map<String, Object> buildResponse(BusinessAccount businessAccount) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toResponseMap(businessAccount));
        return response;
    }

    private Map<String, Object> toResponseMap(BusinessAccount businessAccount) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", businessAccount.getId());
        map.put("code", businessAccount.getCode());
        map.put("companyName", businessAccount.getCompanyName());
        map.put("contactPhone", businessAccount.getContactPhone());
        map.put("logoImageUrl", businessAccount.getLogoImageUrl());
        map.put("websiteUrl", businessAccount.getWebsiteUrl());
        map.put("registrationStatus", businessAccount.getRegistrationStatus());
        map.put("maxUserCount", businessAccount.getMaxUserCount());
        map.put("currentUserCount", businessAccount.getCurrentUserCount());
        map.put("createDate", businessAccount.getCreateDate());
        map.put("lastUpdateDate", businessAccount.getLastUpdateDate());
        map.put("isActive", businessAccount.getIsActive());
        return map;
    }
}
