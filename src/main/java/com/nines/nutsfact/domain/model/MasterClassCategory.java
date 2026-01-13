package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * クラス分類マスタ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterClassCategory implements Serializable {

    private Integer categoryId;
    private Integer categoryType;   // 1:原材料 2:仕込品 3:半製品 4:製品
    private String categoryName;
    private String description;
}
