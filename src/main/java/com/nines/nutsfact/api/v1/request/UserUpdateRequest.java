package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotNull(message = "ユーザーIDは必須です")
    private Integer userId;

    @Size(max = 128, message = "ユーザー名は128文字以内で入力してください")
    private String name;

    private Integer role;

    @Size(max = 256, message = "プロフィール画像URLは256文字以内で入力してください")
    private String profileImageUrl;

    @Size(max = 20, message = "電話番号は20文字以内で入力してください")
    private String phone;

    private Boolean isActive;
}
