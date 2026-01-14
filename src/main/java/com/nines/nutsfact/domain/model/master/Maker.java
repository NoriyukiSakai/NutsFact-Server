package com.nines.nutsfact.domain.model.master;

import lombok.Data;

@Data
public class Maker {
    private Integer makerId;
    private Integer businessAccountId;
    private String makerName;
    private String contactInfo;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
}
