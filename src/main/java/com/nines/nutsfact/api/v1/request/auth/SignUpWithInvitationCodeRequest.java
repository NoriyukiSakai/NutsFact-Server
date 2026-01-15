package com.nines.nutsfact.api.v1.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpWithInvitationCodeRequest {
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String password;

    // 既存ユーザーの場合は空でも可（サービス層で新規ユーザーのみバリデーション）
    private String name;

    @NotBlank(message = "招待コードは必須です")
    @Size(min = 12, max = 12, message = "招待コードは12桁です")
    private String invitationCode;
}
