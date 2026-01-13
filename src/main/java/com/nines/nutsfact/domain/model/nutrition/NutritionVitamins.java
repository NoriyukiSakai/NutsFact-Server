package com.nines.nutsfact.domain.model.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ビタミン成分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionVitamins implements Serializable {

    // ビタミンA関連
    private Float ret;          // レチノール(μg)
    private Integer retFlag;

    private Float carta;        // αカロテン(μg)
    private Integer cartaFlag;

    private Float cartb;        // βカロテン(μg)
    private Integer cartbFlag;

    private Float crypxb;       // βクリプトキサンチン(μg)
    private Integer crypxbFlag;

    private Float cartbeq;      // βカロテン当量(μg)
    private Integer cartbeqFlag;

    private Float vitaRae;      // レチノール活性当量(μg)
    private Integer vitaRaeFlag;

    // ビタミンD
    private Float vitd;         // ビタミンD(μg)
    private Integer vitdFlag;

    // ビタミンE (トコフェロール)
    private Float tocpha;       // αトコフェロール(mg)
    private Integer tocphaFlag;

    private Float tocphb;       // βトコフェロール(mg)
    private Integer tocphbFlag;

    private Float tocphg;       // γトコフェロール(mg)
    private Integer tocphgFlag;

    private Float tocphd;       // δトコフェロール(mg)
    private Integer tocphdFlag;

    // ビタミンK
    private Float vitk;         // ビタミンK(μg)
    private Integer vitkFlag;

    // ビタミンB群
    private Float thia;         // ビタミンB1(mg)
    private Integer thiaFlag;

    private Float ribf;         // ビタミンB2(mg)
    private Integer ribfFlag;

    private Float nia;          // ナイアシン(mg)
    private Integer niaFlag;

    private Float niac;         // ナイアシン当量(mg)
    private Integer niacFlag;

    private Float vitb6a;       // ビタミンB6(mg)
    private Integer vitb6aFlag;

    private Float vitb12;       // ビタミンB12(μg)
    private Integer vitb12Flag;

    private Float fol;          // 葉酸(μg)
    private Integer folFlag;

    private Float pantac;       // パントテン酸(mg)
    private Integer pantacFlag;

    private Float biot;         // ビオチン(μg)
    private Integer biotFlag;

    // ビタミンC
    private Float vitc;         // ビタミンC(mg)
    private Integer vitcFlag;
}
