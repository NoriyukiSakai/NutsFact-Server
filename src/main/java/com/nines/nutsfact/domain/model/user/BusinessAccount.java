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
    private Integer registrationStatus;
    private Integer maxUserCount;
    private Integer currentUserCount;
    private LocalDateTime createDate;
    private LocalDateTime lastUpdateDate;
    private Boolean isActive;
}
