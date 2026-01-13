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
    private Double purchasePrice;
    private Double volumeAmount;
    private Integer weightOrCapacity;
    private Double convertRatio;
    private Double pricePerUnit;
    private Date createDate;
    private Date lastUpdateDate;
    private Boolean isActive;
}
