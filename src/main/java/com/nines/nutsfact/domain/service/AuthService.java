package com.nines.nutsfact.domain.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.JwtUtil;
import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.model.auth.AuthUser;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.InvitationCode;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.repository.BusinessAccountRepository;
import com.nines.nutsfact.domain.repository.InvitationCodeRepository;
import com.nines.nutsfact.domain.repository.UserRepository;
import com.nines.nutsfact.exception.AuthenticationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult signUp(String email, String password, String name) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthenticationException("このメールアドレスは既に登録されています");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setProvider("email");
        user.setIsActive(true);
        userRepository.save(user);

        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        return new AuthResult(authUser, token, user, null, false, "ユーザー登録が完了しました");
    }

    @Transactional
    public AuthResult signIn(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("メールアドレスまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("メールアドレスまたはパスワードが正しくありません");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationException("このアカウントは無効化されています");
        }

        userRepository.updateLastSignInAt(user.getUserId());

        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        return new AuthResult(authUser, token, user, null, false, null);
    }

    @Transactional
    public AuthResult signInWithOAuth(String provider, String idToken, String oauthAccessToken) {
        String authUserId = validateOAuthToken(provider, idToken);
        String email = extractEmailFromOAuthToken(provider, idToken);

        User existingUser = userRepository.findByAuthUserId(authUserId).orElse(null);
        boolean isNewUser = existingUser == null;

        User user;
        if (isNewUser) {
            user = new User();
            user.setAuthUserId(authUserId);
            user.setEmail(email);
            user.setProvider(provider);
            user.setIsActive(true);
            userRepository.save(user);
        } else {
            user = existingUser;
            userRepository.updateLastSignInAt(user.getUserId());
        }

        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        return new AuthResult(authUser, token, user, null, isNewUser, null);
    }

    public AuthToken refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AuthenticationException("リフレッシュトークンが無効です");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("リフレッシュトークンではありません");
        }

        Integer userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("ユーザーが見つかりません"));

        return jwtUtil.generateTokens(user);
    }

    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("ユーザーが見つかりません"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public CurrentUserResult getCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("ユーザーが見つかりません"));

        AuthUser authUser = createAuthUser(user);

        BusinessAccount businessAccount = null;
        if (user.getBusinessAccountId() != null) {
            businessAccount = businessAccountRepository.findById(user.getBusinessAccountId())
                    .orElse(null);
        }

        return new CurrentUserResult(authUser, user, businessAccount);
    }

    @Transactional
    public AuthResult useInvitationCode(Integer userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("ユーザーが見つかりません"));

        InvitationCode invitationCode = invitationCodeRepository.findByCode(code)
                .orElseThrow(() -> new AuthenticationException("招待コードが見つかりません"));

        if (invitationCode.getIsUsed()) {
            throw new AuthenticationException("この招待コードは既に使用されています");
        }

        if (invitationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("この招待コードは有効期限が切れています");
        }

        user.setBusinessAccountId(invitationCode.getBusinessAccountId());
        user.setRole(invitationCode.getRole());
        userRepository.save(user);

        invitationCodeRepository.markAsUsed(invitationCode.getId());

        BusinessAccount businessAccount = businessAccountRepository.findById(invitationCode.getBusinessAccountId())
                .orElse(null);

        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        return new AuthResult(authUser, token, user, businessAccount, false, "招待コードの適用が完了しました");
    }

    @Transactional
    public AuthResult signUpWithInvitationCode(String email, String password, String name, String invitationCode) {
        // 1. 招待コード検証（コードとメールアドレスの組み合わせで検索）
        InvitationCode invitation = invitationCodeRepository.findByCodeAndEmail(invitationCode, email)
                .orElseThrow(() -> new AuthenticationException("招待コードが無効です。コードとメールアドレスを確認してください"));

        // 2. 使用済みチェック
        if (invitation.getIsUsed()) {
            throw new AuthenticationException("この招待コードは既に使用されています");
        }

        // 3. 有効期限チェック
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("この招待コードは有効期限が切れています");
        }

        // 4. メール重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthenticationException("このメールアドレスは既に登録されています");
        }

        // 5. ユーザー作成（BusinessAccountとロールを招待コードから設定）
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setProvider("email");
        user.setBusinessAccountId(invitation.getBusinessAccountId());
        user.setRole(invitation.getRole());
        user.setIsActive(true);
        userRepository.save(user);

        // 6. 招待コードを使用済みにマーク
        invitationCodeRepository.markAsUsed(invitation.getId());

        // 7. JWTトークン生成
        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        // 8. BusinessAccount情報を取得
        BusinessAccount businessAccount = businessAccountRepository.findById(invitation.getBusinessAccountId())
                .orElse(null);

        return new AuthResult(authUser, token, user, businessAccount, false, "ユーザー登録が完了しました");
    }

    private AuthUser createAuthUser(User user) {
        return AuthUser.builder()
                .id(user.getAuthUserId() != null ? user.getAuthUserId() : String.valueOf(user.getUserId()))
                .email(user.getEmail())
                .phone(user.getPhone())
                .lastSignInAt(user.getLastSignInAt())
                .provider(user.getProvider())
                .createdAt(user.getCreatedDate())
                .updatedAt(user.getLastUpdateDate())
                .build();
    }

    private String validateOAuthToken(String provider, String idToken) {
        return "oauth-user-" + provider + "-" + idToken.hashCode();
    }

    private String extractEmailFromOAuthToken(String provider, String idToken) {
        return "oauth-" + provider + "@example.com";
    }

    public record AuthResult(
            AuthUser authUser,
            AuthToken token,
            User user,
            BusinessAccount businessAccount,
            boolean isNewUser,
            String message
    ) {}

    public record CurrentUserResult(
            AuthUser authUser,
            User user,
            BusinessAccount businessAccount
    ) {}
}
