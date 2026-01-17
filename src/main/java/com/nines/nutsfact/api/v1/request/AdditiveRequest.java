package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdditiveRequest {
    private Integer additiveId;

    @Size(max = 32, message = "添加物コードは32文字以内で入力してください")
    private String additiveCode;

    @NotBlank(message = "物質名は必須です")
    @Size(max = 128, message = "物質名は128文字以内で入力してください")
    private String substanceName;

    @Size(max = 128, message = "簡略名は128文字以内で入力してください")
    private String simplifiedName;

    private Integer purposeCategory = 0;

    private Integer collectiveName;

    private Boolean requiresPurposeDisplay = false;

    private String description;

    private Boolean isActive = true;
}
