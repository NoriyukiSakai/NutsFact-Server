package com.nines.nutsfact.api.v1.controller;

import java.util.ArrayList;
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

import com.nines.nutsfact.api.v1.request.ChangeRoleRequest;
import com.nines.nutsfact.api.v1.request.UserCreateRequest;
import com.nines.nutsfact.api.v1.request.UserUpdateRequest;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.service.BusinessAccountService;
import com.nines.nutsfact.domain.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/User")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BusinessAccountService businessAccountService;

    @GetMapping("/GetData")
    public ResponseEntity<Map<String, Object>> getData(Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        User currentUser = userService.findById(userId);

        List<User> users = userService.findByBusinessAccountId(currentUser.getBusinessAccountId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", users.size());
        response.put("item", users);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("userId") Integer userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(buildUserResponse(user));
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();
        user.setBusinessAccountId(request.getBusinessAccountId());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setIsActive(true);

        User created = userService.create(user);
        businessAccountService.updateCurrentUserCount(request.getBusinessAccountId());

        return ResponseEntity.ok(buildUserResponse(created));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody UserUpdateRequest request) {
        User existing = userService.findById(request.getUserId());

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getRole() != null) {
            existing.setRole(request.getRole());
        }
        if (request.getProfileImageUrl() != null) {
            existing.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        User updated = userService.update(request.getUserId(), existing);
        return ResponseEntity.ok(buildUserResponse(updated));
    }

    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam("userId") Integer userId) {
        User user = userService.findById(userId);
        Integer businessAccountId = user.getBusinessAccountId();

        userService.delete(userId);

        if (businessAccountId != null) {
            businessAccountService.updateCurrentUserCount(businessAccountId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivate(@RequestParam("userId") Integer userId) {
        userService.deactivate(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activate(@RequestParam("userId") Integer userId) {
        userService.activate(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/changeRole")
    public ResponseEntity<Map<String, Object>> changeRole(@Valid @RequestBody ChangeRoleRequest request) {
        User updated = userService.changeRole(request.getUserId(), request.getRole());
        return ResponseEntity.ok(buildUserResponse(updated));
    }

    @GetMapping("/getRoleSelect")
    public ResponseEntity<Map<String, Object>> getRoleSelect() {
        List<Map<String, Object>> roles = new ArrayList<>();
        roles.add(Map.of("value", 0, "label", "本部管理者"));
        roles.add(Map.of("value", 1, "label", "オーナー"));
        roles.add(Map.of("value", 2, "label", "管理者"));
        roles.add(Map.of("value", 3, "label", "一般ユーザ"));
        roles.add(Map.of("value", 4, "label", "ゲスト"));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", roles.size());
        response.put("item", roles);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("userId", user.getUserId());
        response.put("businessAccountId", user.getBusinessAccountId());
        response.put("authUserId", user.getAuthUserId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole());
        response.put("profileImageUrl", user.getProfileImageUrl());
        response.put("lastLoginAt", user.getLastSignInAt());
        response.put("createDate", user.getCreatedDate());
        response.put("lastUpdateDate", user.getLastUpdateDate());
        response.put("isActive", user.getIsActive());
        return response;
    }
}
