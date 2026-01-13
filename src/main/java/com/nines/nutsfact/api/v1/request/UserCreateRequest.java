package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotNull(message = "ビジネスアカウントIDは必須です")
    private Integer businessAccountId;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 128, message = "メールアドレスは128文字以内で入力してください")
    private String email;

    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 128, message = "ユーザー名は128文字以内で入力してください")
    private String name;

    @NotNull(message = "ユーザー権限は必須です")
    private Integer role;

    @Size(max = 256, message = "プロフィール画像URLは256文字以内で入力してください")
    private String profileImageUrl;
}
