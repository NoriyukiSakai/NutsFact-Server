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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SystemParameterService systemParameterService;
    private final BusinessAccountService businessAccountService;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;

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

        // ユーザーのアクティブ状態チェック
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Login attempt for inactive user: {}", email);
            throw new AuthenticationException("このアカウントは無効化されています");
        }

        // 所属ビジネスアカウントのアクティブ状態チェック
        if (user.getBusinessAccountId() != null) {
            businessAccountRepository.findById(user.getBusinessAccountId())
                    .ifPresent(account -> {
                        if (!Boolean.TRUE.equals(account.getIsActive())) {
                            log.warn("Login attempt for user with inactive business account: email={}, businessAccountId={}",
                                    email, user.getBusinessAccountId());
                            throw new AuthenticationException("所属する組織が無効化されているため、ログインできません");
                        }
                    });
        }

        // ロックアウトチェック
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Login attempt for locked user: {}", email);
            throw new AuthenticationException("アカウントがロックされています。しばらく経ってから再試行してください。");
        }

        // パスワード検証
        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleLoginFailure(user);
            throw new AuthenticationException("メールアドレスまたはパスワードが正しくありません");
        }

        // ログイン成功：失敗カウントをリセット
        userRepository.resetLoginFailureCount(user.getUserId());
        userRepository.updateLastSignInAt(user.getUserId());

        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        return new AuthResult(authUser, token, user, null, false, null);
    }

    /**
     * ログイン失敗時の処理
     */
    private void handleLoginFailure(User user) {
        int currentFailureCount = user.getLoginFailureCount() != null ? user.getLoginFailureCount() : 0;
        int newCount = currentFailureCount + 1;
        int threshold = systemParameterService.getLoginFailureLockoutThreshold();

        if (newCount >= threshold) {
            // ロックアウト
            int lockoutMinutes = systemParameterService.getLockoutDurationMinutes();
            LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
            userRepository.lockUser(user.getUserId(), lockedUntil);
            log.warn("User locked out: email={}, failureCount={}, lockedUntil={}", user.getEmail(), newCount, lockedUntil);
        } else {
            // 失敗カウント増加
            userRepository.incrementLoginFailureCount(user.getUserId());
            log.info("Login failure recorded: email={}, failureCount={}", user.getEmail(), newCount);
        }
    }

    @Transactional
    public AuthResult signInWithOAuth(String provider, String idToken, String oauthAccessToken) {
        GoogleIdTokenVerifierService.GoogleUserInfo googleUserInfo = verifyGoogleIdToken(provider, idToken);
        String authUserId = "google-" + googleUserInfo.sub();
        String email = googleUserInfo.email();
        String name = googleUserInfo.name();

        User existingUser = userRepository.findByAuthUserId(authUserId).orElse(null);
        boolean isNewUser = existingUser == null;

        User user;
        if (isNewUser) {
            // 新規ユーザーはOAuth単体では登録不可（招待コードが必要）
            throw new AuthenticationException("ユーザーが登録されていません。「Googleアカウントで新規登録」から招待コードを使用して登録してください。");
        } else {
            user = existingUser;

            // ユーザーのアクティブ状態チェック
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                log.warn("OAuth login attempt for inactive user: {}", email);
                throw new AuthenticationException("このアカウントは無効化されています");
            }

            // 所属ビジネスアカウントのアクティブ状態チェック
            if (user.getBusinessAccountId() != null) {
                businessAccountRepository.findById(user.getBusinessAccountId())
                        .ifPresent(account -> {
                            if (!Boolean.TRUE.equals(account.getIsActive())) {
                                log.warn("OAuth login attempt for user with inactive business account: email={}", email);
                                throw new AuthenticationException("所属する組織が無効化されているため、ログインできません");
                            }
                        });
            }

            userRepository.updateLastSignInAt(user.getUserId());
        }

        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        // ビジネスアカウント情報を取得
        BusinessAccount businessAccount = null;
        if (user.getBusinessAccountId() != null) {
            businessAccount = businessAccountRepository.findById(user.getBusinessAccountId()).orElse(null);
        }

        return new AuthResult(authUser, token, user, businessAccount, isNewUser, null);
    }

    /**
     * OAuth + 招待コードでの新規登録
     */
    @Transactional
    public AuthResult signInWithOAuthAndInvitationCode(String provider, String idToken, String oauthAccessToken, String invitationCode) {
        // 1. Google ID Token検証
        GoogleIdTokenVerifierService.GoogleUserInfo googleUserInfo = verifyGoogleIdToken(provider, idToken);
        String authUserId = "google-" + googleUserInfo.sub();
        String email = googleUserInfo.email();
        String name = googleUserInfo.name();

        // 2. 招待コード検証（コードとメールアドレスの組み合わせ）
        InvitationCode invitation = invitationCodeRepository.findByCodeAndEmail(invitationCode, email)
                .orElseThrow(() -> new AuthenticationException("招待コードが無効です。コードとGoogleアカウントのメールアドレスを確認してください"));

        // 3. 使用済みチェック
        if (invitation.getIsUsed()) {
            throw new AuthenticationException("この招待コードは既に使用されています");
        }

        // 4. 有効期限チェック
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("この招待コードは有効期限が切れています");
        }

        // 5. 既存ユーザーチェック（authUserIdで）
        if (userRepository.findByAuthUserId(authUserId).isPresent()) {
            throw new AuthenticationException("このGoogleアカウントは既に登録されています");
        }

        // 6. 既存ユーザーチェック（メールアドレスで）
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthenticationException("このメールアドレスは既に登録されています");
        }

        // 7. ユーザー作成（BusinessAccountとロールを招待コードから設定）
        User user = new User();
        user.setAuthUserId(authUserId);
        user.setEmail(email);
        user.setName(name);
        user.setProvider(provider);
        user.setBusinessAccountId(invitation.getBusinessAccountId());
        user.setRole(invitation.getRole());
        user.setIsActive(true);
        userRepository.save(user);

        // 8. 招待コードを使用済みにマーク
        invitationCodeRepository.markAsUsed(invitation.getId());

        // 9. ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitation.getBusinessAccountId());

        // 10. JWTトークン生成
        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        // 11. BusinessAccount情報を取得
        BusinessAccount businessAccount = businessAccountRepository.findById(invitation.getBusinessAccountId())
                .orElse(null);

        log.info("User registered with OAuth + invitation code: email={}, provider={}, businessAccountId={}",
                email, provider, invitation.getBusinessAccountId());

        return new AuthResult(authUser, token, user, businessAccount, false, "Googleアカウントでの登録が完了しました");
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

        // ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitationCode.getBusinessAccountId());

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

        // 7. ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitation.getBusinessAccountId());

        // 8. JWTトークン生成
        AuthToken token = jwtUtil.generateTokens(user);
        AuthUser authUser = createAuthUser(user);

        // 9. BusinessAccount情報を取得
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

    /**
     * Google ID Tokenを検証
     */
    private GoogleIdTokenVerifierService.GoogleUserInfo verifyGoogleIdToken(String provider, String idToken) {
        if (!"google".equals(provider)) {
            throw new AuthenticationException("サポートされていない認証プロバイダーです: " + provider);
        }
        return googleIdTokenVerifierService.verify(idToken);
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
