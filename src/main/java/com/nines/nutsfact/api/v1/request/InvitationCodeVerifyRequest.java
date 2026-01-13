package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InvitationCodeVerifyRequest {
    @NotBlank(message = "招待コードは必須です")
    @Size(min = 12, max = 12, message = "招待コードは12桁です")
    private String code;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;
}
