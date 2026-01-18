package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 原材料リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRawMaterialRequest {

    private Integer foodId;

    @NotBlank(message = "食品番号は必須です")
    private String foodNo;

    private Integer foodGroupId;
    private String indexNo;
    private Integer classCategoryId;

    // オリジナル参照情報
    private Integer originalFoodId;
    private Integer originalFoodGroupId;
    private String originalFoodNo;
    private String originalIndexNo;
    private String originalFoodName;

    // 食品名・分類
    @NotBlank(message = "食品名は必須です")
    private String foodName;
    private String foodFukuBunrui;
    private String foodRuiKubun;
    private String foodDaiBunrui;
    private String foodCyuBunrui;
    private String foodSyoBunrui;
    private String foodSaibun;

    // カテゴリ・タグ
    private Integer categoryId;
    private String hashtag;

    // 価格・取引先
    private Boolean compositeRawMaterialsKb;
    private Float pricePerUnit;
    private Float lastPricePerUnit;
    private Integer makerId;
    private String makerName;
    private Integer sallerId;
    private String sallerName;

    // 表示情報
    private String compositeRawItemlist;
    private String displayName;
    private String placeOfOrigin;
    private String displayPlaceOfOrigin;

    // 履歴・状態管理
    private Integer revisionOfFoodNo;
    private Integer nextFoodId;
    private Date expireDate;
    private Integer status;
    private String updateInformation;
    private String description;
    private Integer descriptionFlag;
    private Boolean isActive;

    // 基本栄養成分
    private Float refuse;
    private Integer refuseFlag;
    private Integer enerc;
    private Integer enercFlag;
    private Integer enercKcal;
    private Integer enercKcalFlag;
    private Float water;
    private Integer waterFlag;
    private Float prot;
    private Integer protFlag;
    private Float protcaa;
    private Integer protcaaFlag;
    private Float fat;
    private Integer fatFlag;
    private Float fatnlea;
    private Integer fatnleaFlag;
    private Float chole;
    private Integer choleFlag;
    private Float chocdf;
    private Integer chocdfFlag;
    private Float choavlm;
    private Integer choavlmFlag;
    private Boolean choavlmMark;
    private Float choavl;
    private Integer choavlFlag;
    private Float choavldf;
    private Integer choavldfFlag;
    private Boolean choavldfMark;
    private Float fib;
    private Integer fibFlag;
    private Float polyl;
    private Integer polylFlag;
    private Float oa;
    private Integer oaFlag;
    private Float ash;
    private Integer ashFlag;
    private Float alc;
    private Integer alcFlag;
    private Float naclEq;
    private Integer naclEqFlag;

    // ミネラル
    private Float na;
    private Integer naFlag;
    private Float k;
    private Integer kFlag;
    private Float ca;
    private Integer caFlag;
    private Float mg;
    private Integer mgFlag;
    private Float p;
    private Integer pFlag;
    private Float fe;
    private Integer feFlag;
    private Float zn;
    private Integer znFlag;
    private Float cu;
    private Integer cuFlag;
    private Float mn;
    private Integer mnFlag;
    private Float idd;
    private Integer iddFlag;
    private Float se;
    private Integer seFlag;
    private Float cr;
    private Integer crFlag;
    private Float mo;
    private Integer moFlag;

    // ビタミン
    private Float ret;
    private Integer retFlag;
    private Float carta;
    private Integer cartaFlag;
    private Float cartb;
    private Integer cartbFlag;
    private Float crypxb;
    private Integer crypxbFlag;
    private Float cartbeq;
    private Integer cartbeqFlag;
    private Float vitaRae;
    private Integer vitaRaeFlag;
    private Float vitd;
    private Integer vitdFlag;
    private Float tocpha;
    private Integer tocphaFlag;
    private Float tocphb;
    private Integer tocphbFlag;
    private Float tocphg;
    private Integer tocphgFlag;
    private Float tocphd;
    private Integer tocphdFlag;
    private Float vitk;
    private Integer vitkFlag;
    private Float thia;
    private Integer thiaFlag;
    private Float ribf;
    private Integer ribfFlag;
    private Float nia;
    private Integer niaFlag;
    private Float niac;
    private Integer niacFlag;
    private Float vitb6a;
    private Integer vitb6aFlag;
    private Float vitb12;
    private Integer vitb12Flag;
    private Float fol;
    private Integer folFlag;
    private Float pantac;
    private Integer pantacFlag;
    private Float biot;
    private Integer biotFlag;
    private Float vitc;
    private Integer vitcFlag;
}
