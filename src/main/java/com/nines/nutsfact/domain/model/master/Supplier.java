package com.nines.nutsfact.domain.model.master;

import lombok.Data;

@Data
public class Supplier {
    private Integer supplierId;
    private Integer businessAccountId;
    private String supplierName;
    private String contactInfo;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
}
