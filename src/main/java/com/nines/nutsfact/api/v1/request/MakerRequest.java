package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MakerRequest {
    private Integer makerId;

    @NotBlank(message = "製造元名は必須です")
    @Size(max = 100, message = "製造元名は100文字以内で入力してください")
    private String makerName;

    private String contactInfo;

    @Size(max = 256, message = "住所は256文字以内で入力してください")
    private String address;

    @Size(max = 32, message = "電話番号は32文字以内で入力してください")
    private String phoneNumber;

    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 128, message = "メールアドレスは128文字以内で入力してください")
    private String email;

    private Boolean isActive = true;
}
