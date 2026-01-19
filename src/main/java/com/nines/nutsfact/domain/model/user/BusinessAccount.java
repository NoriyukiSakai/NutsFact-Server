package com.nines.nutsfact.domain.model.user;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BusinessAccount {
    private Integer id;
    private String code;
    private String companyName;
    private String contactPhone;
    private String logoImageUrl;
    private String websiteUrl;

    // ラベル用販売者・製造者情報
    private String labelSellerName;
    private String labelSellerAddress;
    private String labelManufacturerName;
    private String labelManufacturerAddress;

    private Integer registrationStatus;
    private Integer maxUserCount;
    private Integer currentUserCount;
    private Boolean isHeadquarters;
    private LocalDateTime createDate;
    private LocalDateTime lastUpdateDate;
    private Boolean isActive;
}
