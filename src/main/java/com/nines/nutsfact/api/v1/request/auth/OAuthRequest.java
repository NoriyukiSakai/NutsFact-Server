package com.nines.nutsfact.api.v1.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthRequest {
    @NotBlank(message = "プロバイダーは必須です")
    private String provider;

    @NotBlank(message = "IDトークンは必須です")
    private String idToken;

    private String accessToken;
}
