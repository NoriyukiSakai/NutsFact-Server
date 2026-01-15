package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.config.JwtUtil;
import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.model.user.UserBusinessAccountMembership;
import com.nines.nutsfact.domain.repository.BusinessAccountRepository;
import com.nines.nutsfact.domain.repository.InvitationCodeRepository;
import com.nines.nutsfact.domain.repository.UserBusinessAccountMembershipRepository;
import com.nines.nutsfact.domain.repository.UserRepository;
import com.nines.nutsfact.exception.AuthenticationException;
import com.nines.nutsfact.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService マルチビジネスアカウント関連機能のユニットテスト
 *
 * テスト対象:
 * - selectBusinessAccount()
 * - getUserMemberships()
 * - signIn() のマルチアカウント対応部分
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceMembershipTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessAccountRepository businessAccountRepository;

    @Mock
    private InvitationCodeRepository invitationCodeRepository;

    @Mock
    private UserBusinessAccountMembershipRepository membershipRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SystemParameterService systemParameterService;

    @Mock
    private BusinessAccountService businessAccountService;

    @Mock
    private GoogleIdTokenVerifierService googleIdTokenVerifierService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private BusinessAccount testBusinessAccount;
    private BusinessAccount testBusinessAccount2;
    private UserBusinessAccountMembership testMembership;
    private UserBusinessAccountMembership testMembership2;
    private AuthToken testToken;

    @BeforeEach
    void setUp() {
        // テストユーザー
        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("encoded-password");
        testUser.setProvider("email");
        testUser.setBusinessAccountId(10);
        testUser.setRole(21);
        testUser.setIsActive(true);

        // テストビジネスアカウント
        testBusinessAccount = new BusinessAccount();
        testBusinessAccount.setId(10);
        testBusinessAccount.setCode("BA001");
        testBusinessAccount.setCompanyName("Test Company");
        testBusinessAccount.setIsActive(true);

        testBusinessAccount2 = new BusinessAccount();
        testBusinessAccount2.setId(20);
        testBusinessAccount2.setCode("BA002");
        testBusinessAccount2.setCompanyName("Test Company 2");
        testBusinessAccount2.setIsActive(true);

        // テストメンバーシップ
        testMembership = new UserBusinessAccountMembership();
        testMembership.setId(1);
        testMembership.setUserId(1);
        testMembership.setBusinessAccountId(10);
        testMembership.setRole(21);
        testMembership.setIsDefault(true);

        testMembership2 = new UserBusinessAccountMembership();
        testMembership2.setId(2);
        testMembership2.setUserId(1);
        testMembership2.setBusinessAccountId(20);
        testMembership2.setRole(10);
        testMembership2.setIsDefault(false);

        // テストトークン
        testToken = AuthToken.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .tokenType("Bearer")
            .expiresIn(3600L)
            .expiresAt(System.currentTimeMillis() / 1000 + 3600)
            .build();
    }

    // ============================================================
    // selectBusinessAccount() テスト
    // ============================================================

    @Nested
    @DisplayName("selectBusinessAccount - アカウント選択")
    class SelectBusinessAccountTests {

        @Test
        @DisplayName("正常系: 有効なビジネスアカウントを選択できる")
        void shouldSelectValidBusinessAccount() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserIdAndBusinessAccountId(1, 20))
                .thenReturn(Optional.of(testMembership2));
            when(businessAccountService.findById(20))
                .thenReturn(testBusinessAccount2);
            when(jwtUtil.generateTokens(any(User.class), eq(20), eq(10)))
                .thenReturn(testToken);
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership, testMembership2));

            // Act
            AuthService.AuthResult result = authService.selectBusinessAccount(1, 20);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.needsAccountSelection()).isFalse();
            assertThat(result.businessAccount()).isEqualTo(testBusinessAccount2);
            assertThat(result.token()).isEqualTo(testToken);

            verify(userRepository).save(any(User.class));
            verify(membershipRepository).setDefaultAccount(1, 20);
        }

        @Test
        @DisplayName("異常系: ユーザーが見つからない場合はAuthenticationException")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.selectBusinessAccount(999, 10))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("ユーザーが見つかりません");

            verify(membershipRepository, never()).findByUserIdAndBusinessAccountId(anyInt(), anyInt());
        }

        @Test
        @DisplayName("異常系: 所属していないアカウントを選択するとAuthenticationException")
        void shouldThrowWhenNotMember() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserIdAndBusinessAccountId(1, 999))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.selectBusinessAccount(1, 999))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("このビジネスアカウントへの所属がありません");
        }

        @Test
        @DisplayName("異常系: ビジネスアカウントが見つからない場合はAuthenticationException")
        void shouldThrowWhenBusinessAccountNotFound() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserIdAndBusinessAccountId(1, 20))
                .thenReturn(Optional.of(testMembership2));
            when(businessAccountService.findById(20))
                .thenThrow(new ResourceNotFoundException("BusinessAccount", 20));

            // Act & Assert
            assertThatThrownBy(() -> authService.selectBusinessAccount(1, 20))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("ビジネスアカウントが見つかりません");
        }

        @Test
        @DisplayName("異常系: 無効化されたアカウントを選択するとAuthenticationException")
        void shouldThrowWhenBusinessAccountIsInactive() {
            // Arrange
            BusinessAccount inactiveAccount = new BusinessAccount();
            inactiveAccount.setId(20);
            inactiveAccount.setIsActive(false);

            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserIdAndBusinessAccountId(1, 20))
                .thenReturn(Optional.of(testMembership2));
            when(businessAccountService.findById(20))
                .thenReturn(inactiveAccount);

            // Act & Assert
            assertThatThrownBy(() -> authService.selectBusinessAccount(1, 20))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("このビジネスアカウントは無効化されています");

            verify(userRepository, never()).save(any());
        }
    }

    // ============================================================
    // getUserMemberships() テスト
    // ============================================================

    @Nested
    @DisplayName("getUserMemberships - 所属一覧取得")
    class GetUserMembershipsTests {

        @Test
        @DisplayName("正常系: ユーザーの所属一覧を取得できる")
        void shouldReturnUserMemberships() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership, testMembership2));
            when(businessAccountService.findById(10))
                .thenReturn(testBusinessAccount);
            when(businessAccountService.findById(20))
                .thenReturn(testBusinessAccount2);

            // Act
            AuthService.MembershipsResult result = authService.getUserMemberships(1);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1);
            assertThat(result.memberships()).hasSize(2);
            assertThat(result.memberships().get(0).businessAccount()).isEqualTo(testBusinessAccount);
            assertThat(result.memberships().get(1).businessAccount()).isEqualTo(testBusinessAccount2);
        }

        @Test
        @DisplayName("正常系: 所属がない場合は空リストを返す")
        void shouldReturnEmptyListWhenNoMemberships() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserId(1)).thenReturn(Collections.emptyList());

            // Act
            AuthService.MembershipsResult result = authService.getUserMemberships(1);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1);
            assertThat(result.memberships()).isEmpty();
        }

        @Test
        @DisplayName("正常系: ビジネスアカウントが見つからない場合はnullを設定")
        void shouldHandleMissingBusinessAccount() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership));
            when(businessAccountService.findById(10))
                .thenThrow(new ResourceNotFoundException("BusinessAccount", 10));

            // Act
            AuthService.MembershipsResult result = authService.getUserMemberships(1);

            // Assert
            assertThat(result.memberships()).hasSize(1);
            assertThat(result.memberships().get(0).businessAccount()).isNull();
        }

        @Test
        @DisplayName("異常系: ユーザーが見つからない場合はAuthenticationException")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.getUserMemberships(999))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("ユーザーが見つかりません");

            verify(membershipRepository, never()).findByUserId(anyInt());
        }
    }

    // ============================================================
    // signIn() マルチアカウント対応テスト
    // ============================================================

    @Nested
    @DisplayName("signIn - マルチアカウント対応")
    class SignInMultiAccountTests {

        @Test
        @DisplayName("単一所属: needsAccountSelection=falseを返す")
        void shouldReturnFalseWhenSingleMembership() {
            // Arrange
            when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership));
            when(businessAccountRepository.findById(10))
                .thenReturn(Optional.of(testBusinessAccount));
            when(jwtUtil.generateTokens(any(User.class), eq(10), eq(21)))
                .thenReturn(testToken);

            // Act
            AuthService.AuthResult result = authService.signIn("test@example.com", "password");

            // Assert
            assertThat(result.needsAccountSelection()).isFalse();
            assertThat(result.memberships()).hasSize(1);
        }

        @Test
        @DisplayName("複数所属: needsAccountSelection=trueを返す")
        void shouldReturnTrueWhenMultipleMemberships() {
            // Arrange
            when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership, testMembership2));
            when(businessAccountRepository.findById(10))
                .thenReturn(Optional.of(testBusinessAccount));
            when(jwtUtil.generateTokens(any(User.class), eq(10), eq(21)))
                .thenReturn(testToken);

            // Act
            AuthService.AuthResult result = authService.signIn("test@example.com", "password");

            // Assert
            assertThat(result.needsAccountSelection()).isTrue();
            assertThat(result.memberships()).hasSize(2);
        }

        @Test
        @DisplayName("所属なし: needsAccountSelection=falseを返す")
        void shouldReturnFalseWhenNoMemberships() {
            // Arrange
            when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);
            when(membershipRepository.findByUserId(1))
                .thenReturn(Collections.emptyList());
            when(jwtUtil.generateTokens(any(User.class)))
                .thenReturn(testToken);

            // Act
            AuthService.AuthResult result = authService.signIn("test@example.com", "password");

            // Assert
            assertThat(result.needsAccountSelection()).isFalse();
            assertThat(result.memberships()).isEmpty();
            assertThat(result.businessAccount()).isNull();
        }

        @Test
        @DisplayName("複数所属でデフォルトアカウントがある場合: デフォルトアカウントでトークン生成")
        void shouldUseDefaultAccountWhenMultipleMemberships() {
            // Arrange
            UserBusinessAccountMembership defaultMembership = new UserBusinessAccountMembership();
            defaultMembership.setId(2);
            defaultMembership.setUserId(1);
            defaultMembership.setBusinessAccountId(20);
            defaultMembership.setRole(10);
            defaultMembership.setIsDefault(true);

            UserBusinessAccountMembership nonDefaultMembership = new UserBusinessAccountMembership();
            nonDefaultMembership.setId(1);
            nonDefaultMembership.setUserId(1);
            nonDefaultMembership.setBusinessAccountId(10);
            nonDefaultMembership.setRole(21);
            nonDefaultMembership.setIsDefault(false);

            when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(nonDefaultMembership, defaultMembership));
            when(businessAccountRepository.findById(20))
                .thenReturn(Optional.of(testBusinessAccount2));
            when(jwtUtil.generateTokens(any(User.class), eq(20), eq(10)))
                .thenReturn(testToken);

            // Act
            AuthService.AuthResult result = authService.signIn("test@example.com", "password");

            // Assert
            assertThat(result.businessAccount()).isEqualTo(testBusinessAccount2);
            verify(jwtUtil).generateTokens(any(User.class), eq(20), eq(10));
        }
    }

    // ============================================================
    // セキュリティ関連テスト
    // ============================================================

    @Nested
    @DisplayName("セキュリティテスト")
    class SecurityTests {

        @Test
        @DisplayName("nullのuserIdでselectBusinessAccountを呼ぶとNullPointerException")
        void shouldHandleNullUserId() {
            // Note: Spring側でバリデーションされるべきだが、念のためテスト
            assertThatThrownBy(() -> authService.selectBusinessAccount(null, 10))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("nullのbusinessAccountIdでselectBusinessAccountを呼ぶとNullPointerException")
        void shouldHandleNullBusinessAccountId() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

            // membershipRepository.findByUserIdAndBusinessAccountIdはnullを受け取れない
            // 実装によっては例外が発生する可能性がある
            assertThatThrownBy(() -> authService.selectBusinessAccount(1, null))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("非アクティブユーザーはログインできない")
        void shouldPreventInactiveUserLogin() {
            // Arrange
            User inactiveUser = new User();
            inactiveUser.setUserId(1);
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setPassword("encoded-password");
            inactiveUser.setIsActive(false);

            when(userRepository.findByEmail("inactive@example.com"))
                .thenReturn(Optional.of(inactiveUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.signIn("inactive@example.com", "password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("このアカウントは無効化されています");
        }

        @Test
        @DisplayName("ロックされたユーザーはログインできない")
        void shouldPreventLockedUserLogin() {
            // Arrange
            User lockedUser = new User();
            lockedUser.setUserId(1);
            lockedUser.setEmail("locked@example.com");
            lockedUser.setPassword("encoded-password");
            lockedUser.setIsActive(true);
            lockedUser.setLockedUntil(LocalDateTime.now().plusHours(1));

            when(userRepository.findByEmail("locked@example.com"))
                .thenReturn(Optional.of(lockedUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.signIn("locked@example.com", "password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("アカウントがロックされています");
        }

        @Test
        @DisplayName("単一所属でもビジネスアカウントが無効化されていればログイン失敗")
        void shouldPreventLoginWhenSingleAccountIsInactive() {
            // Arrange
            BusinessAccount inactiveAccount = new BusinessAccount();
            inactiveAccount.setId(10);
            inactiveAccount.setIsActive(false);

            when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership));
            when(businessAccountRepository.findById(10))
                .thenReturn(Optional.of(inactiveAccount));

            // Act & Assert
            assertThatThrownBy(() -> authService.signIn("test@example.com", "password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("無効化されている");
        }
    }

    // ============================================================
    // トランザクション境界テスト
    // ============================================================

    @Nested
    @DisplayName("トランザクション境界テスト")
    class TransactionTests {

        @Test
        @DisplayName("selectBusinessAccount: ユーザー更新とデフォルト設定が両方実行される")
        void shouldUpdateBothUserAndDefaultAccount() {
            // Arrange
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(membershipRepository.findByUserIdAndBusinessAccountId(1, 20))
                .thenReturn(Optional.of(testMembership2));
            when(businessAccountService.findById(20))
                .thenReturn(testBusinessAccount2);
            when(jwtUtil.generateTokens(any(User.class), eq(20), eq(10)))
                .thenReturn(testToken);
            when(membershipRepository.findByUserId(1))
                .thenReturn(List.of(testMembership, testMembership2));

            // Act
            authService.selectBusinessAccount(1, 20);

            // Assert - 両方の更新が実行されることを確認
            verify(userRepository).save(argThat(user ->
                user.getBusinessAccountId() == 20 && user.getRole() == 10
            ));
            verify(membershipRepository).setDefaultAccount(1, 20);
        }
    }
}
