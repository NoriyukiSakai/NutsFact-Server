package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessAccountCreateRequest {
    @NotBlank(message = "会社名は必須です")
    @Size(max = 100, message = "会社名は100文字以内で入力してください")
    private String companyName;

    @NotBlank(message = "連絡先電話番号は必須です")
    @Size(max = 32, message = "連絡先電話番号は32文字以内で入力してください")
    private String contactPhone;

    @Size(max = 256, message = "ロゴ画像URLは256文字以内で入力してください")
    private String logoImageUrl;

    @Size(max = 256, message = "WebサイトURLは256文字以内で入力してください")
    private String websiteUrl;

    private Integer maxUserCount = 3;
}
