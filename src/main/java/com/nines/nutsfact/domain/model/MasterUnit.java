package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 単位マスタ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterUnit implements Serializable {

    private Integer unitId;
    private String unitName;
    private Integer unitType;
}
