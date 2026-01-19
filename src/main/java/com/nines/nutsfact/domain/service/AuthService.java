package com.nines.nutsfact.domain.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.JwtUtil;
import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.model.auth.AuthUser;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.InvitationCode;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.model.user.UserBusinessAccountMembership;
import com.nines.nutsfact.domain.repository.BusinessAccountRepository;
import com.nines.nutsfact.domain.repository.InvitationCodeRepository;
import com.nines.nutsfact.domain.repository.UserBusinessAccountMembershipRepository;
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
    private final UserBusinessAccountMembershipRepository membershipRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SystemParameterService systemParameterService;
    private final BusinessAccountService businessAccountService;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;

    @Transactional
    public AuthResult signIn(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("メールアドレスまたはパスワードが正しくありません"));

        // ユーザーのアクティブ状態チェック
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Login attempt for inactive user: {}", email);
            throw new AuthenticationException("このアカウントは無効化されています");
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

        // 所属ビジネスアカウント一覧を取得
        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(user.getUserId());
        AuthUser authUser = createAuthUser(user);

        if (memberships.isEmpty()) {
            // 所属なし（未所属ユーザー）はログイン不可
            log.warn("Login rejected: user {} has no business account membership", user.getEmail());
            throw new AuthenticationException("ビジネスアカウントに所属していないため、ログインできません。管理者にお問い合わせください。");
        } else if (memberships.size() == 1) {
            // 単一所属：自動的にそのアカウントを選択
            UserBusinessAccountMembership membership = memberships.get(0);
            BusinessAccount account = businessAccountRepository.findById(membership.getBusinessAccountId()).orElse(null);

            // ビジネスアカウントのアクティブ状態チェック
            if (account != null && !Boolean.TRUE.equals(account.getIsActive())) {
                throw new AuthenticationException("所属する組織が無効化されているため、ログインできません");
            }

            AuthToken token = jwtUtil.generateTokens(user, membership.getBusinessAccountId(), membership.getRole());
            return new AuthResult(authUser, token, user, account, false, null, memberships, false);
        } else {
            // 複数所属：アカウント選択が必要
            // トークンは一時的なもの（アカウント選択前）
            UserBusinessAccountMembership defaultMembership = memberships.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsDefault()))
                    .findFirst()
                    .orElse(memberships.get(0));
            BusinessAccount defaultAccount = businessAccountRepository.findById(defaultMembership.getBusinessAccountId()).orElse(null);
            AuthToken token = jwtUtil.generateTokens(user, defaultMembership.getBusinessAccountId(), defaultMembership.getRole());
            return new AuthResult(authUser, token, user, defaultAccount, false, null, memberships, true);
        }
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

        User existingUser = userRepository.findByAuthUserId(authUserId).orElse(null);

        if (existingUser == null) {
            // 新規ユーザーはOAuth単体では登録不可（招待コードが必要）
            throw new AuthenticationException("ユーザーが登録されていません。「Googleアカウントで新規登録」から招待コードを使用して登録してください。");
        }

        User user = existingUser;

        // ユーザーのアクティブ状態チェック
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("OAuth login attempt for inactive user: {}", email);
            throw new AuthenticationException("このアカウントは無効化されています");
        }

        userRepository.updateLastSignInAt(user.getUserId());

        // 所属ビジネスアカウント一覧を取得
        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(user.getUserId());
        AuthUser authUser = createAuthUser(user);

        if (memberships.isEmpty()) {
            // 所属なし（未所属ユーザー）はログイン不可
            log.warn("OAuth login rejected: user {} has no business account membership", user.getEmail());
            throw new AuthenticationException("ビジネスアカウントに所属していないため、ログインできません。管理者にお問い合わせください。");
        } else if (memberships.size() == 1) {
            // 単一所属：自動的にそのアカウントを選択
            UserBusinessAccountMembership membership = memberships.get(0);
            BusinessAccount account = businessAccountRepository.findById(membership.getBusinessAccountId()).orElse(null);

            // ビジネスアカウントのアクティブ状態チェック
            if (account != null && !Boolean.TRUE.equals(account.getIsActive())) {
                throw new AuthenticationException("所属する組織が無効化されているため、ログインできません");
            }

            AuthToken token = jwtUtil.generateTokens(user, membership.getBusinessAccountId(), membership.getRole());
            return new AuthResult(authUser, token, user, account, false, null, memberships, false);
        } else {
            // 複数所属：アカウント選択が必要
            UserBusinessAccountMembership defaultMembership = memberships.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsDefault()))
                    .findFirst()
                    .orElse(memberships.get(0));
            BusinessAccount defaultAccount = businessAccountRepository.findById(defaultMembership.getBusinessAccountId()).orElse(null);
            AuthToken token = jwtUtil.generateTokens(user, defaultMembership.getBusinessAccountId(), defaultMembership.getRole());
            return new AuthResult(authUser, token, user, defaultAccount, false, null, memberships, true);
        }
    }

    /**
     * OAuth + 招待コードでの新規登録、または既存Googleユーザーの新団体紐付け
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
        User existingUserByAuthId = userRepository.findByAuthUserId(authUserId).orElse(null);
        if (existingUserByAuthId != null) {
            // 既存のGoogleユーザーが存在する場合：auth_user一致で本人確認済み、新団体に紐付け
            return handleExistingGoogleUserWithInvitation(existingUserByAuthId, invitation);
        }

        // 6. 既存ユーザーチェック（メールアドレスで）
        User existingUserByEmail = userRepository.findByEmail(email).orElse(null);
        if (existingUserByEmail != null) {
            // メールアドレスは既に登録されているが、別のproviderで登録されている
            throw new AuthenticationException(
                "このメールアドレスは別の認証方法（" + existingUserByEmail.getProvider() + "）で登録されています。" +
                "該当の認証方法でログイン後、招待コードを使用してください。");
        }

        // 7. 新規ユーザー作成（BusinessAccountとロールを招待コードから設定）
        User user = new User();
        user.setAuthUserId(authUserId);
        user.setEmail(email);
        user.setName(name);
        user.setProvider(provider);
        user.setBusinessAccountId(invitation.getBusinessAccountId());
        user.setRole(invitation.getRole());
        user.setIsActive(true);
        userRepository.save(user);

        // 8. Membershipレコードを作成
        UserBusinessAccountMembership membership = new UserBusinessAccountMembership();
        membership.setUserId(user.getUserId());
        membership.setBusinessAccountId(invitation.getBusinessAccountId());
        membership.setRole(invitation.getRole());
        membership.setIsDefault(true);
        membershipRepository.save(membership);

        // 9. 招待コードを使用済みにマーク
        invitationCodeRepository.markAsUsed(invitation.getId());

        // 10. ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitation.getBusinessAccountId());

        // 11. JWTトークン生成
        AuthToken token = jwtUtil.generateTokens(user, membership.getBusinessAccountId(), membership.getRole());
        AuthUser authUser = createAuthUser(user);

        // 12. BusinessAccount情報を取得
        BusinessAccount businessAccount = businessAccountRepository.findById(invitation.getBusinessAccountId())
                .orElse(null);

        log.info("New user registered with OAuth + invitation code: email={}, provider={}, businessAccountId={}",
                email, provider, invitation.getBusinessAccountId());

        return new AuthResult(authUser, token, user, businessAccount, true, "Googleアカウントでの登録が完了しました", List.of(membership), false);
    }

    /**
     * 既存のGoogleユーザーを招待コードで新しいビジネスアカウントに紐付け
     * auth_user_id一致で本人確認済みとして扱う
     */
    private AuthResult handleExistingGoogleUserWithInvitation(User existingUser, InvitationCode invitation) {
        // ユーザーのアクティブ状態チェック
        if (!Boolean.TRUE.equals(existingUser.getIsActive())) {
            throw new AuthenticationException("このアカウントは無効化されています");
        }

        // 既にこのビジネスアカウントに所属しているかチェック
        if (membershipRepository.findByUserIdAndBusinessAccountId(
                existingUser.getUserId(), invitation.getBusinessAccountId()).isPresent()) {
            throw new AuthenticationException("既にこのビジネスアカウントに所属しています");
        }

        // Membershipレコードを作成（既存の所属を維持しつつ新しい所属を追加）
        UserBusinessAccountMembership membership = new UserBusinessAccountMembership();
        membership.setUserId(existingUser.getUserId());
        membership.setBusinessAccountId(invitation.getBusinessAccountId());
        membership.setRole(invitation.getRole());
        membership.setIsDefault(false); // 既存ユーザーなので新しい所属はデフォルトにしない
        membershipRepository.save(membership);

        // 招待コードを使用済みにマーク
        invitationCodeRepository.markAsUsed(invitation.getId());

        // ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitation.getBusinessAccountId());

        // 最終ログイン時刻を更新
        userRepository.updateLastSignInAt(existingUser.getUserId());

        // 更新後の所属一覧を取得
        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(existingUser.getUserId());

        // 新しく追加したアカウントでトークンを生成
        AuthToken token = jwtUtil.generateTokens(existingUser, membership.getBusinessAccountId(), membership.getRole());
        AuthUser authUser = createAuthUser(existingUser);

        BusinessAccount businessAccount = businessAccountRepository.findById(invitation.getBusinessAccountId())
                .orElse(null);

        log.info("Existing Google user linked to new business account: userId={}, email={}, newBusinessAccountId={}",
                existingUser.getUserId(), existingUser.getEmail(), invitation.getBusinessAccountId());

        return new AuthResult(authUser, token, existingUser, businessAccount, false,
                "新しいビジネスアカウントへの参加が完了しました", memberships, memberships.size() > 1);
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

        // 所属ビジネスアカウントを確認
        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(userId);
        if (memberships.isEmpty()) {
            log.warn("Token refresh rejected: user {} has no business account membership", user.getEmail());
            throw new AuthenticationException("ビジネスアカウントに所属していないため、セッションを継続できません。再度ログインしてください。");
        }

        // デフォルトまたは最初のメンバーシップを使用
        UserBusinessAccountMembership membership = memberships.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDefault()))
                .findFirst()
                .orElse(memberships.get(0));

        return jwtUtil.generateTokens(user, membership.getBusinessAccountId(), membership.getRole());
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
            // BusinessAccountServiceを通して取得（ユーザー数が動的に計算される）
            try {
                businessAccount = businessAccountService.findById(user.getBusinessAccountId());
            } catch (Exception e) {
                log.warn("Failed to fetch business account: {}", user.getBusinessAccountId(), e);
                businessAccount = null;
            }
        }

        return new CurrentUserResult(authUser, user, businessAccount);
    }

    /**
     * 招待コードを使用して新しいビジネスアカウントに参加
     * 既存ユーザーが新しいビジネスアカウントに所属する場合に使用
     */
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

        // 既にこのビジネスアカウントに所属しているかチェック
        if (membershipRepository.findByUserIdAndBusinessAccountId(userId, invitationCode.getBusinessAccountId()).isPresent()) {
            throw new AuthenticationException("既にこのビジネスアカウントに所属しています");
        }

        // Membershipレコードを作成（既存の所属を維持しつつ新しい所属を追加）
        UserBusinessAccountMembership membership = new UserBusinessAccountMembership();
        membership.setUserId(userId);
        membership.setBusinessAccountId(invitationCode.getBusinessAccountId());
        membership.setRole(invitationCode.getRole());
        // 初めての所属の場合のみデフォルトに設定
        boolean isFirstMembership = membershipRepository.countByUserId(userId) == 0;
        membership.setIsDefault(isFirstMembership);
        membershipRepository.save(membership);

        // ユーザーの現在のビジネスアカウントを更新（初めての所属の場合）
        if (isFirstMembership) {
            user.setBusinessAccountId(invitationCode.getBusinessAccountId());
            user.setRole(invitationCode.getRole());
            userRepository.save(user);
        }

        invitationCodeRepository.markAsUsed(invitationCode.getId());

        // ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitationCode.getBusinessAccountId());

        BusinessAccount businessAccount = businessAccountRepository.findById(invitationCode.getBusinessAccountId())
                .orElse(null);

        // 更新後の所属一覧を取得
        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(userId);

        // 新しく追加したアカウントでトークンを生成
        AuthToken token = jwtUtil.generateTokens(user, membership.getBusinessAccountId(), membership.getRole());
        AuthUser authUser = createAuthUser(user);

        return new AuthResult(authUser, token, user, businessAccount, false, "招待コードの適用が完了しました", memberships, memberships.size() > 1);
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

        // 4. 既存ユーザーチェック
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            // 既存ユーザーが存在する場合
            return handleExistingEmailUserWithInvitation(existingUser, password, invitation);
        }

        // 5. 新規ユーザーの場合はユーザー名を必須チェック
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }

        // 6. 新規ユーザー作成（BusinessAccountとロールを招待コードから設定）
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setProvider("email");
        user.setBusinessAccountId(invitation.getBusinessAccountId());
        user.setRole(invitation.getRole());
        user.setIsActive(true);
        userRepository.save(user);

        // 6. Membershipレコードを作成
        UserBusinessAccountMembership membership = new UserBusinessAccountMembership();
        membership.setUserId(user.getUserId());
        membership.setBusinessAccountId(invitation.getBusinessAccountId());
        membership.setRole(invitation.getRole());
        membership.setIsDefault(true);
        membershipRepository.save(membership);

        // 7. 招待コードを使用済みにマーク
        invitationCodeRepository.markAsUsed(invitation.getId());

        // 8. ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitation.getBusinessAccountId());

        // 9. JWTトークン生成
        AuthToken token = jwtUtil.generateTokens(user, membership.getBusinessAccountId(), membership.getRole());
        AuthUser authUser = createAuthUser(user);

        // 10. BusinessAccount情報を取得
        BusinessAccount businessAccount = businessAccountRepository.findById(invitation.getBusinessAccountId())
                .orElse(null);

        log.info("New user registered with invitation code: email={}, businessAccountId={}",
                email, invitation.getBusinessAccountId());

        return new AuthResult(authUser, token, user, businessAccount, true, "ユーザー登録が完了しました", List.of(membership), false);
    }

    /**
     * 既存のemailユーザーを招待コードで新しいビジネスアカウントに紐付け
     * パスワード照合による本人確認を行う
     */
    private AuthResult handleExistingEmailUserWithInvitation(User existingUser, String password, InvitationCode invitation) {
        // provider="email" 以外の場合はエラー
        if (!"email".equals(existingUser.getProvider())) {
            throw new AuthenticationException(
                "このメールアドレスは別の認証方法（" + existingUser.getProvider() + "）で登録されています。" +
                "該当の認証方法でログイン後、招待コードを使用してください。");
        }

        // ユーザーのアクティブ状態チェック
        if (!Boolean.TRUE.equals(existingUser.getIsActive())) {
            throw new AuthenticationException("このアカウントは無効化されています");
        }

        // ロックアウトチェック
        if (existingUser.getLockedUntil() != null && existingUser.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AuthenticationException("アカウントがロックされています。しばらく経ってから再試行してください。");
        }

        // パスワード照合による本人確認
        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            handleLoginFailure(existingUser);
            throw new AuthenticationException("パスワードが正しくありません");
        }

        // ログイン成功：失敗カウントをリセット
        userRepository.resetLoginFailureCount(existingUser.getUserId());

        // 既にこのビジネスアカウントに所属しているかチェック
        if (membershipRepository.findByUserIdAndBusinessAccountId(
                existingUser.getUserId(), invitation.getBusinessAccountId()).isPresent()) {
            throw new AuthenticationException("既にこのビジネスアカウントに所属しています");
        }

        // Membershipレコードを作成（既存の所属を維持しつつ新しい所属を追加）
        UserBusinessAccountMembership membership = new UserBusinessAccountMembership();
        membership.setUserId(existingUser.getUserId());
        membership.setBusinessAccountId(invitation.getBusinessAccountId());
        membership.setRole(invitation.getRole());
        membership.setIsDefault(false); // 既存ユーザーなので新しい所属はデフォルトにしない
        membershipRepository.save(membership);

        // 招待コードを使用済みにマーク
        invitationCodeRepository.markAsUsed(invitation.getId());

        // ビジネスアカウントのユーザー数を更新
        businessAccountService.updateCurrentUserCount(invitation.getBusinessAccountId());

        // 更新後の所属一覧を取得
        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(existingUser.getUserId());

        // 新しく追加したアカウントでトークンを生成
        AuthToken token = jwtUtil.generateTokens(existingUser, membership.getBusinessAccountId(), membership.getRole());
        AuthUser authUser = createAuthUser(existingUser);

        BusinessAccount businessAccount = businessAccountRepository.findById(invitation.getBusinessAccountId())
                .orElse(null);

        log.info("Existing email user linked to new business account: userId={}, email={}, newBusinessAccountId={}",
                existingUser.getUserId(), existingUser.getEmail(), invitation.getBusinessAccountId());

        return new AuthResult(authUser, token, existingUser, businessAccount, false,
                "新しいビジネスアカウントへの参加が完了しました", memberships, memberships.size() > 1);
    }

    /**
     * ビジネスアカウントを選択（アカウント切り替え）
     * ログイン後のアカウント選択、またはアカウント切り替え時に使用
     */
    @Transactional
    public AuthResult selectBusinessAccount(Integer userId, Integer businessAccountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("ユーザーが見つかりません"));

        // 選択したビジネスアカウントへの所属を確認
        UserBusinessAccountMembership membership = membershipRepository
                .findByUserIdAndBusinessAccountId(userId, businessAccountId)
                .orElseThrow(() -> new AuthenticationException("このビジネスアカウントへの所属がありません"));

        // ビジネスアカウントのアクティブ状態チェック（ServiceからはResourceNotFoundExceptionがスローされる）
        BusinessAccount businessAccount;
        try {
            businessAccount = businessAccountService.findById(businessAccountId);
        } catch (Exception e) {
            throw new AuthenticationException("ビジネスアカウントが見つかりません");
        }

        if (!Boolean.TRUE.equals(businessAccount.getIsActive())) {
            throw new AuthenticationException("このビジネスアカウントは無効化されています");
        }

        // ユーザーの現在のビジネスアカウントを更新
        user.setBusinessAccountId(businessAccountId);
        user.setRole(membership.getRole());
        userRepository.save(user);

        // デフォルトアカウントを更新
        membershipRepository.setDefaultAccount(userId, businessAccountId);

        // 選択されたアカウントでトークンを生成
        AuthToken token = jwtUtil.generateTokens(user, businessAccountId, membership.getRole());
        AuthUser authUser = createAuthUser(user);

        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(userId);

        log.info("Business account switched: userId={}, businessAccountId={}", userId, businessAccountId);

        return new AuthResult(authUser, token, user, businessAccount, false, null, memberships, false);
    }

    /**
     * ユーザーの所属ビジネスアカウント一覧を取得
     */
    public MembershipsResult getUserMemberships(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("ユーザーが見つかりません"));

        List<UserBusinessAccountMembership> memberships = membershipRepository.findByUserId(userId);

        // 各メンバーシップにビジネスアカウント情報を付与（ユーザー数を動的に計算）
        List<MembershipWithAccount> membershipWithAccounts = memberships.stream()
                .map(m -> {
                    BusinessAccount account;
                    try {
                        account = businessAccountService.findById(m.getBusinessAccountId());
                    } catch (Exception e) {
                        account = null;
                    }
                    return new MembershipWithAccount(m, account);
                })
                .toList();

        return new MembershipsResult(userId, membershipWithAccounts);
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
            String message,
            List<UserBusinessAccountMembership> memberships,
            boolean needsAccountSelection
    ) {
        // 後方互換性のためのファクトリメソッド
        public static AuthResult of(AuthUser authUser, AuthToken token, User user,
                                     BusinessAccount businessAccount, boolean isNewUser, String message) {
            return new AuthResult(authUser, token, user, businessAccount, isNewUser, message, List.of(), false);
        }
    }

    public record CurrentUserResult(
            AuthUser authUser,
            User user,
            BusinessAccount businessAccount
    ) {}

    public record MembershipWithAccount(
            UserBusinessAccountMembership membership,
            BusinessAccount businessAccount
    ) {}

    public record MembershipsResult(
            Integer userId,
            List<MembershipWithAccount> memberships
    ) {}
}
