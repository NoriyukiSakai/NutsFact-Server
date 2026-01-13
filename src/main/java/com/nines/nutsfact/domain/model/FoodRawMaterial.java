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

/**
 * 原材料エンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRawMaterial implements Serializable {

    // === 識別情報 ===
    private Integer foodId;
    private String foodNo;
    private Integer foodGroupId;
    private String indexNo;
    private Integer classCategoryId;

    // === オリジナル参照情報（成分表からの参照） ===
    private Integer originalFoodId;
    private Integer originalFoodGroupId;
    private String originalFoodNo;
    private String originalIndexNo;
    private String originalFoodName;

    // === 食品名・分類 ===
    private String foodName;
    private String foodFukuBunrui;      // 副分類
    private String foodRuiKubun;        // 類区分
    private String foodDaiBunrui;       // 大分類
    private String foodCyuBunrui;       // 中分類
    private String foodSyoBunrui;       // 小分類
    private String foodSaibun;          // 細分

    // === カテゴリ・タグ ===
    private Integer categoryId;
    private String hashtag;

    // === 価格・取引先情報 ===
    private Boolean compositeRawMaterialsKb;    // 複合原材料フラグ
    private Float pricePerUnit;                  // 100グラム単価
    private Integer makerId;
    private String makerName;
    private Integer sellerId;
    private String sellerName;

    // === 表示情報 ===
    private String compositeRawItemlist;        // 複合原材料の原材料表記
    private String displayName;
    private String placeOfOrigin;
    private String displayPlaceOfOrigin;

    // === 履歴・状態管理 ===
    private Integer revisionOfFoodNo;
    private Integer nextFoodId;
    private Date expireDate;
    private Date createDate;
    private Date lastUpdateDate;
    private Integer status;
    private String updateInformation;
    private String description;
    private Integer descriptionFlag;
    private Boolean isActive;

    // === 栄養成分（コンポジション） ===
    private NutritionBasic basicNutrition;
    private NutritionMinerals minerals;
    private NutritionVitamins vitamins;
}
