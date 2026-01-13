package com.nines.nutsfact.config;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContextから認証情報を取得するヘルパークラス
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        // ユーティリティクラスのためインスタンス化不可
    }

    /**
     * 現在の認証済みユーザーを取得
     * @return AuthenticatedUser（未認証の場合はOptional.empty()）
     */
    public static Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser) {
            return Optional.of((AuthenticatedUser) principal);
        }

        return Optional.empty();
    }

    /**
     * 現在のユーザーIDを取得
     * @return userId（未認証の場合はnull）
     */
    public static Integer getCurrentUserId() {
        return getCurrentUser().map(AuthenticatedUser::getUserId).orElse(null);
    }

    /**
     * 現在のビジネスアカウントIDを取得
     * @return businessAccountId（未認証または未設定の場合はnull）
     */
    public static Integer getCurrentBusinessAccountId() {
        return getCurrentUser().map(AuthenticatedUser::getBusinessAccountId).orElse(null);
    }

    /**
     * 現在のユーザーがビジネスアカウントに所属しているか
     * @return ビジネスアカウントに所属している場合true
     */
    public static boolean hasBusinessAccount() {
        return getCurrentUser().map(AuthenticatedUser::hasBusinessAccount).orElse(false);
    }

    /**
     * 認証済みユーザーを取得（必須）
     * @return AuthenticatedUser
     * @throws IllegalStateException 未認証の場合
     */
    public static AuthenticatedUser requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("認証が必要です"));
    }

    /**
     * ビジネスアカウントIDを取得（必須）
     * @return businessAccountId
     * @throws IllegalStateException 未認証またはビジネスアカウント未設定の場合
     */
    public static Integer requireBusinessAccountId() {
        AuthenticatedUser user = requireCurrentUser();
        if (!user.hasBusinessAccount()) {
            throw new IllegalStateException("ビジネスアカウントへの所属が必要です");
        }
        return user.getBusinessAccountId();
    }
}
