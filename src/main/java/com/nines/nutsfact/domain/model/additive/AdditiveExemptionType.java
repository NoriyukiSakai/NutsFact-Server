package com.nines.nutsfact.domain.model.additive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 添加物表示免除区分
 * 食品表示基準に基づく、表示が免除される条件
 */
@Getter
@RequiredArgsConstructor
public enum AdditiveExemptionType {
    REQUIRED(0, "表示必要", "通常表示が必要"),
    CARRYOVER(1, "キャリーオーバー", "最終製品で効果を発揮しない"),
    PROCESSING_AID(2, "加工助剤", "製造工程で除去または中和される"),
    NUTRIENT_FORTIFIER(3, "栄養強化剤", "栄養強化目的で使用");

    private final int code;
    private final String displayName;
    private final String description;

    /**
     * コードから免除区分を取得
     */
    public static AdditiveExemptionType fromCode(int code) {
        for (AdditiveExemptionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return REQUIRED;
    }

    /**
     * 表示が免除されるかどうか
     */
    public static boolean isExempted(int code) {
        return code != REQUIRED.code;
    }

    /**
     * 表示名を取得（コード指定）
     */
    public static String getDisplayName(int code) {
        return fromCode(code).getDisplayName();
    }

    /**
     * 説明を取得（コード指定）
     */
    public static String getDescription(int code) {
        return fromCode(code).getDescription();
    }
}
