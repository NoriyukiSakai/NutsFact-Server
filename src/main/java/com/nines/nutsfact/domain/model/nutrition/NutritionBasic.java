package com.nines.nutsfact.domain.model.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 基本栄養成分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionBasic implements Serializable {

    private Float refuse;           // 廃棄率(%)
    private Integer refuseFlag;

    private Integer enerc;          // エネルギー(kJ)
    private Integer enercFlag;

    private Integer enercKcal;      // エネルギー(kcal)
    private Integer enercKcalFlag;

    private Float water;            // 水分(g)
    private Integer waterFlag;

    private Float prot;             // たんぱく質(g)
    private Integer protFlag;

    private Float protcaa;          // アミノ酸組成によるたんぱく質(g)
    private Integer protcaaFlag;

    private Float fat;              // 脂質(g)
    private Integer fatFlag;

    private Float fatnlea;          // 脂肪酸のトリアシルグリセロール当量(g)
    private Integer fatnleaFlag;

    private Float chole;            // コレステロール(mg)
    private Integer choleFlag;

    private Float chocdf;           // 炭水化物(g)
    private Integer chocdfFlag;

    private Float choavlm;          // 利用可能炭水化物：単糖当量(g)
    private Integer choavlmFlag;
    private Boolean choavlmMark;

    private Float choavl;           // 利用可能炭水化物：質量計(g)
    private Integer choavlFlag;

    private Float choavldf;         // 差引き法による利用可能炭水化物(g)
    private Integer choavldfFlag;
    private Boolean choavldfMark;

    private Float fib;              // 食物繊維総量(g)
    private Integer fibFlag;

    private Float polyl;            // 糖アルコール(g)
    private Integer polylFlag;

    private Float oa;               // 有機酸(g)
    private Integer oaFlag;

    private Float ash;              // 灰分(g)
    private Integer ashFlag;

    private Float alc;              // アルコール(g)
    private Integer alcFlag;

    private Float naclEq;           // 食塩相当量(g)
    private Integer naclEqFlag;
}
