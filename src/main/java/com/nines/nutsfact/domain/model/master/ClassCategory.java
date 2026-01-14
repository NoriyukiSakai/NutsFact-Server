package com.nines.nutsfact.domain.model.master;

import lombok.Data;

@Data
public class ClassCategory {
    private Integer categoryId;
    private Integer businessAccountId;
    private Integer categoryType; // 1:原材料 2:仕込品 3:半製品 4:製品
    private String categoryName;
    private String description;
    private Boolean isActive;
}
