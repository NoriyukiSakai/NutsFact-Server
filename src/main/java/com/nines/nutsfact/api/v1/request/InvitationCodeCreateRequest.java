package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvitationCodeCreateRequest {
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    // roleを指定しない場合はビジネス利用者（21）がデフォルト
    private Integer role;

    private Integer expirationDays = 7;
}
