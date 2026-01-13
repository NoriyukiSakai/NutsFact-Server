package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessAccountUpdateRequest {
    @NotNull(message = "ビジネスアカウントIDは必須です")
    private Integer id;

    @Size(max = 100, message = "会社名は100文字以内で入力してください")
    private String companyName;

    @Size(max = 32, message = "連絡先電話番号は32文字以内で入力してください")
    private String contactPhone;

    @Size(max = 256, message = "ロゴ画像URLは256文字以内で入力してください")
    private String logoImageUrl;

    @Size(max = 256, message = "WebサイトURLは256文字以内で入力してください")
    private String websiteUrl;

    private Integer maxUserCount;
}
