package com.nines.nutsfact.domain.model.master;

import lombok.Data;

@Data
public class Seller {
    private Integer sellerId;
    private String sellerName;
    private String contactInfo;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
}
