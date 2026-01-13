package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 食品群マスタ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterFoodGroup implements Serializable {

    private Integer foodGroupId;
    private String foodGroupName;
    private String description;
}
