package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvitationCodeCreateRequest {
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotNull(message = "ユーザー権限は必須です")
    private Integer role;

    private Integer expirationDays = 7;
}
