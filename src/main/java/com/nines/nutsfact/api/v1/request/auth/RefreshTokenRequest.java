package com.nines.nutsfact.api.v1.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;
}
