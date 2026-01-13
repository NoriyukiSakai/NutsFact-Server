package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 原材料仕入先情報エンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRawMaterialSupplier {
    private Integer id;
    private Integer foodId;
    private Integer supplierId;
    private String supplierName;
    private Integer purchasePrice;
    private Integer volumeAmount;
    private Integer weightOrCapacity;
    private Float convertRatio;
    private Float pricePerUnit;
    private Date createDate;
    private Date lastUpdateDate;
    private Boolean isActive;
}
