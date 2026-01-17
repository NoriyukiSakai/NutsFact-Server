package com.nines.nutsfact.domain.model.additive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 原材料別添加物エンティティ
 * 原材料に割り当てられた添加物情報
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialAdditive implements Serializable {

    private Integer id;
    private Integer businessAccountId;
    private Integer foodId;             // 原材料ID
    private Integer additiveId;         // 添加物ID
    private Integer displayOrder;       // 表示順
    private Float usageAmount;          // 使用量(g/100g)
    private Integer exemptionType;      // 表示免除区分（AdditiveExemptionType）
    private String exemptionReason;     // 免除理由
    private String allergenOrigin;      // アレルゲン由来（例：大豆由来）
    private Boolean isActive;
    private Date createDate;
    private Date updateDate;

    // === 関連エンティティ（JOINで取得） ===
    private Additive additive;

    /**
     * 表示免除区分Enumを取得
     */
    public AdditiveExemptionType getExemptionTypeEnum() {
        return AdditiveExemptionType.fromCode(exemptionType != null ? exemptionType : 0);
    }

    /**
     * 表示が必要かどうか
     */
    public boolean isDisplayRequired() {
        return !AdditiveExemptionType.isExempted(exemptionType != null ? exemptionType : 0);
    }

    /**
     * 表示文字列を生成
     */
    public String toDisplayString() {
        if (additive == null) {
            return "";
        }

        String baseDisplay = additive.toDisplayString();

        // アレルゲン由来があれば追加
        if (allergenOrigin != null && !allergenOrigin.isEmpty()) {
            return baseDisplay + "（" + allergenOrigin + "）";
        }

        return baseDisplay;
    }
}
