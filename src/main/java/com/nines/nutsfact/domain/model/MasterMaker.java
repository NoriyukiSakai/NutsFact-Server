package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 製造元マスタ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterMaker implements Serializable {

    private Integer makerId;
    private String makerName;
    private String contactInfo;
}
