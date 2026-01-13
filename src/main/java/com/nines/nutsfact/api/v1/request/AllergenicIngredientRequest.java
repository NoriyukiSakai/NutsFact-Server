package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AllergenicIngredientRequest {
    @NotBlank(message = "アレルゲン成分名は必須です")
    @Size(max = 100, message = "アレルゲン成分名は100文字以内で入力してください")
    private String allergenicIngredientName;

    private String description;

    @Size(max = 50, message = "カテゴリは50文字以内で入力してください")
    private String category;
}
