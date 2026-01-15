package com.nines.nutsfact.domain.model.user;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
    private Integer userId;
    private Integer businessAccountId;
    private String authUserId;
    private String email;
    private String name;
    private Integer role;
    private String profileImageUrl;
    private String phone;
    private String password;
    private String provider;
    private LocalDateTime lastSignInAt;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdateDate;
    private Boolean isActive;
    private Integer loginFailureCount;
    private LocalDateTime lockedUntil;
}
