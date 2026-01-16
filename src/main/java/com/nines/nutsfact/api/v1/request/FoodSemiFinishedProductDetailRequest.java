package com.nines.nutsfact.api.v1.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 半完成品明細リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSemiFinishedProductDetailRequest {

    private Integer detailId;
    private Integer semiId;

    // 構成品区分
    private Boolean componentKb;
    private Integer detailFoodId;
    private Integer detailPreId;
    private Boolean compositeRawMaterialsKb;

    // 名称
    private String detailFoodName;
    private String detailPreName;

    // 配合情報
    private Float mixingRatio;
    private Float weight;
    private Float costPrice;

    // 栄養成分（表示用6項目）
    private Float energy;
    private Float protein;
    private Float fat;
    private Float carbo;
    private Float sugar;
    private Float sodium;

    private Boolean isActive;
}
