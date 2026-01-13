package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitRequest {
    @NotBlank(message = "単位名は必須です")
    @Size(max = 100, message = "単位名は100文字以内で入力してください")
    private String unitName;

    @NotNull(message = "単位タイプは必須です")
    private Integer unitType;
}
