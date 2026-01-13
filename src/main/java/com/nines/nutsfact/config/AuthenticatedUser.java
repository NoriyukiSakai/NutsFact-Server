package com.nines.nutsfact.config;

import java.security.Principal;

import lombok.Builder;
import lombok.Getter;

/**
 * 認証済みユーザー情報
 * JWTトークンから取得した情報をSecurityContextで保持するためのPrincipal
 */
@Getter
@Builder
public class AuthenticatedUser implements Principal {

    private final Integer userId;
    private final Integer businessAccountId;
    private final String email;
    private final Integer role;

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    /**
     * ビジネスアカウントIDが設定されているか
     */
    public boolean hasBusinessAccount() {
        return businessAccountId != null;
    }
}
