package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 仕込品明細エンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodPreProductDetailItem implements Serializable {

    private Integer detailId;
    private Integer preId;

    // 構成品区分
    private Boolean componentKb;                // true:仕込品 false:原材料
    private Integer detailFoodId;               // 原材料ID
    private Integer detailPreId;                // 仕込品ID
    private Boolean compositeRawMaterialsKb;    // 複合原材料区分

    // 名称
    private String detailFoodName;
    private String detailPreName;

    // 数量・計算値
    private Float mixingRatio;                  // 配合比率
    private Float weight;                       // 重量
    private Float costPrice;                    // 原価

    // 栄養成分
    private Float energy;
    private Float protein;
    private Float fat;
    private Float carbo;
    private Float sugar;
    private Float sodium;
}
