package com.nines.nutsfact.domain.model;

import com.nines.nutsfact.domain.model.nutrition.NutritionBasic;
import com.nines.nutsfact.domain.model.nutrition.NutritionMinerals;
import com.nines.nutsfact.domain.model.nutrition.NutritionVitamins;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 半完成品エンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSemiFinishedProduct implements Serializable {

    private Integer semiId;
    private Integer businessAccountId;
    private String semiNo;
    private String semiName;
    private String displayName;
    private Integer classCategoryId;

    // 内容量・単位
    private Float capacity;
    private Integer unit;                   // 0:g, 1:ml, 2:個

    // 重量入力モード
    private Integer weightInputMode;        // 0:配合率, 1:重量直接入力

    // 栄養成分表示設定
    private Integer infUnit;                // 0:100g当たり 1:1個当たり
    private Float infVolume;
    private Integer infDisplay;             // 表記種別（0:目安, 1:推定値, 2:成分表計算, 3:分析値）
    private Float infEnergy;
    private Float infProtein;
    private Float infFat;
    private Float infCarbo;
    private Float infSugar;
    private Float infSodium;

    // 栄養成分（全51項目・コンポジション）
    private NutritionBasic basicNutrition;
    private NutritionMinerals minerals;
    private NutritionVitamins vitamins;

    // 期限表示
    private Integer infLmtKind;             // 0:賞味期限 1:消費期限
    private Boolean infLmtDateFlag;         // 日付指定フラグ
    private Date infLmtDate;                // 期限日付
    private Integer infLmtDays;             // 期限日数（製造日から）
    private String infStorageMethod;

    // コンタミネーション
    private Boolean infContamiFlag;
    private String infContamination;

    // アレルゲン集約情報（JSON形式）
    private String allergenSummary;

    // 合計値
    private Float weightSum;
    private Float costPriceSum;
    private Integer detailCount;

    // その他
    private String placeOfOrigin;
    private String purpose;
    private Boolean isActive;
    private Date createDate;
    private Date updateDate;

    // 明細（関連）
    private List<FoodSemiFinishedProductDetail> details;
}
