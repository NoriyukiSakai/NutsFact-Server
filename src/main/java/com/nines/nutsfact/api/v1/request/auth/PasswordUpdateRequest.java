package com.nines.nutsfact.api.v1.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank(message = "新しいパスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String newPassword;
}
