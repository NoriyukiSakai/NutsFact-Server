package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 複合原材料の使用原材料情報エンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompositeRawMaterialIngredient {
    private Integer id;
    private Integer businessAccountId;
    private Integer foodId;
    private Integer displayOrder;
    private Integer ingredientFoodId;
    private String ingredientName;
    private Double ratio;
    private Boolean isActive;
    private Date createDate;
    private Date lastUpdateDate;
}
