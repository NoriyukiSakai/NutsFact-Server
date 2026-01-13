package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassCategoryRequest {
    @NotNull(message = "区分タイプは必須です")
    private Integer categoryType; // 1:原材料 2:仕込品 3:半製品 4:製品

    @NotBlank(message = "カテゴリ名は必須です")
    @Size(max = 100, message = "カテゴリ名は100文字以内で入力してください")
    private String categoryName;

    private String description;
}
