package com.nines.nutsfact.domain.model.user;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * ユーザービジネスアカウント所属エンティティ
 * 同一ユーザーが複数のビジネスアカウントに所属するための中間テーブル
 */
@Data
public class UserBusinessAccountMembership {
    private Integer id;
    private Integer userId;
    private Integer businessAccountId;
    /**
     * ビジネスアカウントでの役割
     * 10: ビジネスオーナー
     * 21: ビジネス利用者
     */
    private Integer role;
    /** デフォルトアカウントフラグ（ログイン時の初期選択） */
    private Boolean isDefault;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
