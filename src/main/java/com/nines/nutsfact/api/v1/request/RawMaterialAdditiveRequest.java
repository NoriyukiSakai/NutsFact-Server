package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RawMaterialAdditiveRequest {
    private Integer id;

    @NotNull(message = "原材料IDは必須です")
    private Integer foodId;

    @NotNull(message = "添加物IDは必須です")
    private Integer additiveId;

    private Integer displayOrder = 0;

    private Float usageAmount;

    private Integer exemptionType = 0;

    private String exemptionReason;

    @Size(max = 128, message = "アレルゲン由来は128文字以内で入力してください")
    private String allergenOrigin;

    private Boolean isActive = true;
}
