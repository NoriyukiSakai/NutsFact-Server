package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 販売者マスタ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterSeller implements Serializable {

    private Integer sellerId;
    private String sellerName;
    private String contactInfo;
}
