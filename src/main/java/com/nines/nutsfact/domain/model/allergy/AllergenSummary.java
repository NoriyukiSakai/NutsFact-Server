package com.nines.nutsfact.domain.model.allergy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * アレルゲン集約情報
 * 半完成品の全構成材料から集計されたアレルゲン情報
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergenSummary {

    /**
     * アレルゲン個別情報
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllergenItem {
        private Integer itemNo;          // アレルゲン番号（1-30）
        private String type;             // アレルゲンタイプ（英語キー）
        private String name;             // アレルゲン名（日本語）
        private Double totalWeight;      // 使用量合計
        private Boolean isMandatory;     // 特定原材料（義務表示）かどうか
    }

    private List<AllergenItem> allergens;  // 使用量順にソートされたリスト
    private LocalDateTime calculatedAt;     // 計算日時

    /**
     * アレルゲン番号から名前を取得
     */
    public static String getAllergenName(int itemNo) {
        return switch (itemNo) {
            case 1 -> "えび";
            case 2 -> "かに";
            case 3 -> "小麦";
            case 4 -> "そば";
            case 5 -> "卵";
            case 6 -> "乳";
            case 7 -> "落花生";
            case 8 -> "アーモンド";
            case 9 -> "あわび";
            case 10 -> "いか";
            case 11 -> "いくら";
            case 12 -> "オレンジ";
            case 13 -> "カシューナッツ";
            case 14 -> "キウイフルーツ";
            case 15 -> "牛肉";
            case 16 -> "くるみ";
            case 17 -> "ごま";
            case 18 -> "さけ";
            case 19 -> "さば";
            case 20 -> "大豆";
            case 21 -> "鶏肉";
            case 22 -> "バナナ";
            case 23 -> "豚肉";
            case 24 -> "まつたけ";
            case 25 -> "もも";
            case 26 -> "やまいも";
            case 27 -> "りんご";
            case 28 -> "ゼラチン";
            case 29 -> "マカダミアナッツ";
            default -> "不明";
        };
    }

    /**
     * アレルゲン番号から英語キーを取得
     */
    public static String getAllergenType(int itemNo) {
        return switch (itemNo) {
            case 1 -> "shrimp";
            case 2 -> "crab";
            case 3 -> "wheat";
            case 4 -> "buckwheat";
            case 5 -> "egg";
            case 6 -> "milk";
            case 7 -> "peanut";
            case 8 -> "almond";
            case 9 -> "abalone";
            case 10 -> "squid";
            case 11 -> "salmon_roe";
            case 12 -> "orange";
            case 13 -> "cashew_nut";
            case 14 -> "kiwifruit";
            case 15 -> "beef";
            case 16 -> "walnut";
            case 17 -> "sesame";
            case 18 -> "salmon";
            case 19 -> "mackerel";
            case 20 -> "soybean";
            case 21 -> "chicken";
            case 22 -> "banana";
            case 23 -> "pork";
            case 24 -> "matsutake";
            case 25 -> "peach";
            case 26 -> "yam";
            case 27 -> "apple";
            case 28 -> "gelatin";
            case 29 -> "macadamia_nut";
            default -> "unknown";
        };
    }

    /**
     * 特定原材料（義務表示）かどうかを判定
     * えび、かに、小麦、そば、卵、乳、落花生、くるみ の8品目
     */
    public static boolean isMandatory(int itemNo) {
        return itemNo == 1 || itemNo == 2 || itemNo == 3 || itemNo == 4 ||
               itemNo == 5 || itemNo == 6 || itemNo == 7 || itemNo == 16;
    }
}
