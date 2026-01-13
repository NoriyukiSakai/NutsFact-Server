package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 仕入先マスタ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterSupplier implements Serializable {

    private Integer supplierId;
    private String supplierName;
    private String contactInfo;
}
