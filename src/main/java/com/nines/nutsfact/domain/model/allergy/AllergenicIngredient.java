package com.nines.nutsfact.domain.model.allergy;

import lombok.Data;

@Data
public class AllergenicIngredient {
    private Integer allergenicIngredientId;
    private String allergenicIngredientName;
    private String description;
    private String category;
}
