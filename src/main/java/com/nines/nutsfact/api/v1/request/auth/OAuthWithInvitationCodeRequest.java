package com.nines.nutsfact.api.v1.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * OAuth + 招待コードでの登録リクエスト
 */
@Data
public class OAuthWithInvitationCodeRequest {
    @NotBlank(message = "プロバイダーは必須です")
    private String provider;

    @NotBlank(message = "IDトークンは必須です")
    private String idToken;

    private String accessToken;

    @NotBlank(message = "招待コードは必須です")
    @Size(min = 12, max = 12, message = "招待コードは12桁です")
    private String invitationCode;
}
