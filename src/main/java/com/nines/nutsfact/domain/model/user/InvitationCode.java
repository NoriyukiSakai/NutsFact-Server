package com.nines.nutsfact.domain.model.user;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InvitationCode {
    private Integer id;
    private Integer businessAccountId;
    private String code;
    private String email;
    private Integer role;
    private LocalDateTime expiresAt;
    private Boolean isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime createDate;
    private Integer createdByUserId;
}
