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
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.service.BusinessAccountService;
import com.nines.nutsfact.domain.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/BusinessAccount")
@RequiredArgsConstructor
public class BusinessAccountController {

    private final BusinessAccountService businessAccountService;
    private final UserService userService;

    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("id") Integer id) {
        BusinessAccount businessAccount = businessAccountService.findById(id);
        return ResponseEntity.ok(buildResponse(businessAccount));
    }

    @GetMapping("/getCurrent")
    public ResponseEntity<Map<String, Object>> getCurrent(Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        User user = userService.findById(userId);

        if (user.getBusinessAccountId() == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Fail");
            response.put("message", "ビジネスアカウントが設定されていません");
            return ResponseEntity.ok(response);
        }

        BusinessAccount businessAccount = businessAccountService.findById(user.getBusinessAccountId());
        return ResponseEntity.ok(buildResponse(businessAccount));
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            Authentication authentication,
            @Valid @RequestBody BusinessAccountCreateRequest request) {
        Integer userId = (Integer) authentication.getPrincipal();

        BusinessAccount businessAccount = new BusinessAccount();
        businessAccount.setCompanyName(request.getCompanyName());
        businessAccount.setContactPhone(request.getContactPhone());
        businessAccount.setLogoImageUrl(request.getLogoImageUrl());
        businessAccount.setWebsiteUrl(request.getWebsiteUrl());
        businessAccount.setMaxUserCount(request.getMaxUserCount());

        BusinessAccount created = businessAccountService.create(businessAccount);

        User user = userService.findById(userId);
        user.setBusinessAccountId(created.getId());
        user.setRole(1);
        userService.update(userId, user);

        businessAccountService.updateCurrentUserCount(created.getId());

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

    private Map<String, Object> buildResponse(BusinessAccount businessAccount) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", businessAccount.getId());
        response.put("code", businessAccount.getCode());
        response.put("companyName", businessAccount.getCompanyName());
        response.put("contactPhone", businessAccount.getContactPhone());
        response.put("logoImageUrl", businessAccount.getLogoImageUrl());
        response.put("websiteUrl", businessAccount.getWebsiteUrl());
        response.put("registrationStatus", businessAccount.getRegistrationStatus());
        response.put("maxUserCount", businessAccount.getMaxUserCount());
        response.put("currentUserCount", businessAccount.getCurrentUserCount());
        response.put("createDate", businessAccount.getCreateDate());
        response.put("lastUpdateDate", businessAccount.getLastUpdateDate());
        response.put("isActive", businessAccount.getIsActive());
        return response;
    }
}
