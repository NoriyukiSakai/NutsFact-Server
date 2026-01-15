package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 食品成分表辞書エンティティ（8訂成分データ格納用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodCompositionDictionary implements Serializable {

    private Integer foodId;
    private Integer foodGroupId;
    private Integer foodNo;
    private String indexNo;
    private String foodName;

    // 分類情報
    private String foodFukuBunrui;  // 副分類
    private String foodRuiKubun;    // 類区分
    private String foodDaiBunrui;   // 大分類
    private String foodCyuBunrui;   // 中分類
    private String foodSyoBunrui;   // 小分類
    private String foodSaibun;      // 細分

    // 基本成分
    private Float refuse;           // 廃棄率(%)
    private Integer enerc;          // エネルギー(kJ)
    private Integer enercKcal;      // エネルギー(kcal)
    private Float water;
    private Integer waterFlag;
    private Float protcaa;
    private Integer protcaaFlag;
    private Float prot;
    private Integer protFlag;
    private Float fatnlea;
    private Integer fatnleaFlag;
    private Float chole;
    private Integer choleFlag;
    private Float fat;
    private Integer fatFlag;

    // 炭水化物
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
    private Float chocdf;
    private Integer chocdfFlag;

    // その他
    private Float oa;
    private Integer oaFlag;
    private Float ash;
    private Integer ashFlag;

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
    private Float id;
    private Integer idFlag;
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

    // その他
    private Float alc;
    private Integer alcFlag;
    private Float naclEq;
    private Integer naclEqFlag;

    private String description;
    private Integer historyId;
    private Boolean isActive;
}
