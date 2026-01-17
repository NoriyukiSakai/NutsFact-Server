package com.nines.nutsfact.domain.model.additive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 添加物一括名（14種類）
 * 食品表示基準に基づく、物質名ではなく一括名で表示できる添加物
 */
@Getter
@RequiredArgsConstructor
public enum AdditiveCollectiveName {
    YEAST_FOOD(1, "イーストフード"),
    GUM_BASE(2, "ガムベース"),
    KANSUI(3, "かんすい"),
    ENZYME(4, "酵素"),
    GLAZING_AGENT(5, "光沢剤"),
    FLAVOR(6, "香料"),
    ACIDULANT(7, "酸味料"),
    CHEWING_GUM_SOFTENER(8, "チューインガム軟化剤"),
    SEASONING(9, "調味料"),
    TOFU_COAGULANT(10, "豆腐用凝固剤"),
    BITTERING_AGENT(11, "苦味料"),
    EMULSIFIER(12, "乳化剤"),
    PH_ADJUSTER(13, "pH調整剤"),
    LEAVENING_AGENT(14, "膨張剤");

    private final int code;
    private final String displayName;

    /**
     * コードから一括名を取得
     */
    public static AdditiveCollectiveName fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AdditiveCollectiveName name : values()) {
            if (name.code == code) {
                return name;
            }
        }
        return null;
    }

    /**
     * 表示名を取得（コード指定）
     */
    public static String getDisplayName(Integer code) {
        AdditiveCollectiveName name = fromCode(code);
        return name != null ? name.getDisplayName() : null;
    }

    /**
     * 一括名として有効なコードか
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
