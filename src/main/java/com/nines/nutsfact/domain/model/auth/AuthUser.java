package com.nines.nutsfact.domain.model.auth;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private String id;
    private String email;
    private LocalDateTime emailConfirmedAt;
    private String phone;
    private LocalDateTime lastSignInAt;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
