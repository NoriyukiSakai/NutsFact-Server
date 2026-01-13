package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FoodGroupRequest {
    private Integer foodGroupId;

    @NotBlank(message = "食品分類名は必須です")
    @Size(max = 100, message = "食品分類名は100文字以内で入力してください")
    private String foodGroupName;

    private String description;
}
