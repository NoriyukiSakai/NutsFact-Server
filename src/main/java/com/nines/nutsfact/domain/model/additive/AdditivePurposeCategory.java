package com.nines.nutsfact.domain.model.additive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 添加物用途区分（用途名併記が必要な8用途）
 * 食品表示基準に基づく
 */
@Getter
@RequiredArgsConstructor
public enum AdditivePurposeCategory {
    OTHER(0, "その他", false),
    SWEETENER(1, "甘味料", true),
    COLORING(2, "着色料", true),
    PRESERVATIVE(3, "保存料", true),
    THICKENER(4, "増粘剤、安定剤、ゲル化剤又は糊料", true),
    ANTIOXIDANT(5, "酸化防止剤", true),
    COLOR_FORMER(6, "発色剤", true),
    BLEACHING_AGENT(7, "漂白剤", true),
    ANTIFUNGAL(8, "防かび剤", true);

    private final int code;
    private final String displayName;
    private final boolean requiresPurposeDisplay;

    /**
     * コードから用途区分を取得
     */
    public static AdditivePurposeCategory fromCode(int code) {
        for (AdditivePurposeCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return OTHER;
    }

    /**
     * 用途名併記が必要かどうか
     */
    public static boolean requiresPurposeDisplay(int code) {
        AdditivePurposeCategory category = fromCode(code);
        return category.isRequiresPurposeDisplay();
    }

    /**
     * 表示名を取得（コード指定）
     */
    public static String getDisplayName(int code) {
        return fromCode(code).getDisplayName();
    }
}
