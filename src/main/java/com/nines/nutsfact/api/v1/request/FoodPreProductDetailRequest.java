package com.nines.nutsfact.api.v1.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仕込品明細リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodPreProductDetailRequest {

    private Integer detailId;
    private Integer preId;

    // 構成品区分
    private Boolean componentKb;
    private Integer detailFoodId;
    private Integer detailPreId;
    private Boolean compositeRawMaterialsKb;

    // 名称
    private String detailFoodName;
    private String detailPreName;

    // 数量・計算値
    private Float mixingRatio;
    private Float weight;
    private Float costPrice;

    // 栄養成分
    private Float energy;
    private Float protein;
    private Float fat;
    private Float carbo;
    private Float sugar;
    private Float sodium;
}
