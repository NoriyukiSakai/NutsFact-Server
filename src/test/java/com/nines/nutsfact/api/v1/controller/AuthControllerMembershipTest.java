package com.nines.nutsfact.api.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nines.nutsfact.config.AuthenticatedUser;
import com.nines.nutsfact.config.JwtUtil;
import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.model.user.UserBusinessAccountMembership;
import com.nines.nutsfact.domain.service.AuthService;
import com.nines.nutsfact.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController マルチビジネスアカウント関連エンドポイントのテスト
 *
 * テスト対象:
 * - GET /apix/Auth/memberships
 * - POST /apix/Auth/selectAccount
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerMembershipTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User testUser;
    private BusinessAccount testBusinessAccount;
    private BusinessAccount testBusinessAccount2;
    private UserBusinessAccountMembership testMembership;
    private UserBusinessAccountMembership testMembership2;
    private String validToken = "valid-test-token";

    @BeforeEach
    void setUp() {
        // テストユーザー作成
        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setProvider("email");
        testUser.setBusinessAccountId(10);
        testUser.setRole(21);
        testUser.setIsActive(true);

        // テストビジネスアカウント作成
        testBusinessAccount = new BusinessAccount();
        testBusinessAccount.setId(10);
        testBusinessAccount.setCode("BA001");
        testBusinessAccount.setCompanyName("Test Company");
        testBusinessAccount.setIsActive(true);
        testBusinessAccount.setIsHeadquarters(false);

        testBusinessAccount2 = new BusinessAccount();
        testBusinessAccount2.setId(20);
        testBusinessAccount2.setCode("BA002");
        testBusinessAccount2.setCompanyName("Test Company 2");
        testBusinessAccount2.setIsActive(true);
        testBusinessAccount2.setIsHeadquarters(false);

        // テストメンバーシップ作成
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

        // JWT検証のモック設定
        setupJwtMock();
    }

    private void setupJwtMock() {
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.isAccessToken(validToken)).thenReturn(true);

        Claims claims = Jwts.claims()
            .subject("1")
            .add("email", "test@example.com")
            .add("businessAccountId", 10)
            .add("role", 21)
            .add("type", "access")
            .build();

        when(jwtUtil.parseToken(validToken)).thenReturn(claims);
    }

    // ============================================================
    // GET /apix/Auth/memberships テスト
    // ============================================================

    @Nested
    @DisplayName("GET /apix/Auth/memberships - 所属一覧取得")
    class GetMembershipsTests {

        @Test
        @DisplayName("正常系: 認証済みユーザーが所属一覧を取得できる")
        void shouldReturnMembershipsForAuthenticatedUser() throws Exception {
            // Arrange
            List<AuthService.MembershipWithAccount> memberships = List.of(
                new AuthService.MembershipWithAccount(testMembership, testBusinessAccount),
                new AuthService.MembershipWithAccount(testMembership2, testBusinessAccount2)
            );
            when(authService.getUserMemberships(1))
                .thenReturn(new AuthService.MembershipsResult(1, memberships));

            // Act & Assert
            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.memberships").isArray())
                .andExpect(jsonPath("$.memberships", hasSize(2)));

            verify(authService).getUserMemberships(1);
        }

        @Test
        @DisplayName("正常系: 所属がない場合は空配列を返す")
        void shouldReturnEmptyArrayWhenNoMemberships() throws Exception {
            // Arrange
            when(authService.getUserMemberships(1))
                .thenReturn(new AuthService.MembershipsResult(1, Collections.emptyList()));

            // Act & Assert
            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.memberships").isArray())
                .andExpect(jsonPath("$.memberships", hasSize(0)));
        }

        @Test
        @DisplayName("正常系: 単一所属の場合も正常に返す")
        void shouldReturnSingleMembership() throws Exception {
            // Arrange
            List<AuthService.MembershipWithAccount> memberships = List.of(
                new AuthService.MembershipWithAccount(testMembership, testBusinessAccount)
            );
            when(authService.getUserMemberships(1))
                .thenReturn(new AuthService.MembershipsResult(1, memberships));

            // Act & Assert
            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberships", hasSize(1)))
                // JSON field names are in snake_case
                .andExpect(jsonPath("$.memberships[0].membership.business_account_id").value(10))
                .andExpect(jsonPath("$.memberships[0].business_account.company_name").value("Test Company"));
        }

        @Test
        @DisplayName("異常系: 認証なしでアクセスすると403エラー")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            // Spring Securityは認証なしのリクエストに対して403 Forbiddenを返す
            mockMvc.perform(get("/apix/Auth/memberships")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("異常系: 無効なトークンでアクセスすると403エラー")
        void shouldReturn403WithInvalidToken() throws Exception {
            // Arrange
            String invalidToken = "invalid-token";
            when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

            // Act & Assert
            // 無効なトークンの場合、認証されず403 Forbiddenが返される
            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + invalidToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

            verifyNoInteractions(authService);
        }
    }

    // ============================================================
    // POST /apix/Auth/selectAccount テスト
    // ============================================================

    @Nested
    @DisplayName("POST /apix/Auth/selectAccount - アカウント選択")
    class SelectAccountTests {

        @Test
        @DisplayName("正常系: 有効なビジネスアカウントを選択できる")
        void shouldSelectValidBusinessAccount() throws Exception {
            // Arrange
            AuthToken newToken = AuthToken.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(System.currentTimeMillis() / 1000 + 3600)
                .build();

            AuthService.AuthResult result = new AuthService.AuthResult(
                null, newToken, testUser, testBusinessAccount2,
                false, null, List.of(testMembership, testMembership2), false
            );
            when(authService.selectBusinessAccount(1, 20)).thenReturn(result);

            Map<String, Integer> request = Map.of("businessAccountId", 20);

            // Act & Assert
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                // JSON field names are in snake_case
                .andExpect(jsonPath("$.token.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.needsAccountSelection").value(false));

            verify(authService).selectBusinessAccount(1, 20);
        }

        @Test
        @DisplayName("異常系: businessAccountIdがnullの場合はエラー")
        void shouldReturnErrorWhenBusinessAccountIdIsNull() throws Exception {
            Map<String, Object> request = Collections.singletonMap("businessAccountId", null);

            // IllegalArgumentException が発生し、500エラーになる
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("異常系: 所属していないアカウントを選択すると401エラー")
        void shouldReturn401WhenSelectingUnaffiliatedAccount() throws Exception {
            // Arrange
            when(authService.selectBusinessAccount(1, 999))
                .thenThrow(new AuthenticationException("このビジネスアカウントへの所属がありません"));

            Map<String, Integer> request = Map.of("businessAccountId", 999);

            // Act & Assert
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("Unauthorized"));
        }

        @Test
        @DisplayName("異常系: 無効化されたアカウントを選択すると401エラー")
        void shouldReturn401WhenSelectingInactiveAccount() throws Exception {
            // Arrange
            when(authService.selectBusinessAccount(1, 30))
                .thenThrow(new AuthenticationException("このビジネスアカウントは無効化されています"));

            Map<String, Integer> request = Map.of("businessAccountId", 30);

            // Act & Assert
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("このビジネスアカウントは無効化されています"));
        }

        @Test
        @DisplayName("異常系: 認証なしでアクセスすると403エラー")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            Map<String, Integer> request = Map.of("businessAccountId", 20);

            // Spring Securityは認証なしのリクエストに対して403 Forbiddenを返す
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

            verifyNoInteractions(authService);
        }
    }

    // ============================================================
    // 入力値バリデーションテスト
    // ============================================================

    @Nested
    @DisplayName("入力値バリデーションテスト")
    class ValidationTests {

        @Test
        @DisplayName("負の数値を送信してもサービス層で処理される")
        void shouldHandleNegativeNumberInBusinessAccountId() throws Exception {
            // Arrange
            when(authService.selectBusinessAccount(anyInt(), eq(-1)))
                .thenThrow(new AuthenticationException("ビジネスアカウントが見つかりません"));

            Map<String, Integer> request = Map.of("businessAccountId", -1);

            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("0を送信してもサービス層で処理される")
        void shouldHandleZeroBusinessAccountId() throws Exception {
            // Arrange
            when(authService.selectBusinessAccount(1, 0))
                .thenThrow(new AuthenticationException("ビジネスアカウントが見つかりません"));

            Map<String, Integer> request = Map.of("businessAccountId", 0);

            // Act & Assert
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("空のリクエストボディはエラー")
        void shouldReturnErrorForEmptyBody() throws Exception {
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().is5xxServerError());

            verifyNoInteractions(authService);
        }
    }

    // ============================================================
    // エッジケーステスト
    // ============================================================

    @Nested
    @DisplayName("エッジケーステスト")
    class EdgeCaseTests {

        @Test
        @DisplayName("同じアカウントを再選択しても正常に処理される")
        void shouldHandleReselectingSameAccount() throws Exception {
            // Arrange
            AuthToken newToken = AuthToken.builder()
                .accessToken("same-account-token")
                .refreshToken("same-account-refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(System.currentTimeMillis() / 1000 + 3600)
                .build();

            AuthService.AuthResult result = new AuthService.AuthResult(
                null, newToken, testUser, testBusinessAccount,
                false, null, List.of(testMembership), false
            );
            when(authService.selectBusinessAccount(1, 10)).thenReturn(result);

            Map<String, Integer> request = Map.of("businessAccountId", 10);

            // Act & Assert
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("運営管理者がアカウントを選択できる")
        void shouldAllowSystemAdminToSelectAccount() throws Exception {
            // Arrange: 運営管理者用のJWTモック設定
            String adminToken = "admin-jwt-token";
            when(jwtUtil.validateToken(adminToken)).thenReturn(true);
            when(jwtUtil.isAccessToken(adminToken)).thenReturn(true);

            Claims adminClaims = Jwts.claims()
                .subject("999")
                .add("email", "admin@example.com")
                .add("businessAccountId", 1)
                .add("role", 0) // systemAdmin
                .add("type", "access")
                .build();
            when(jwtUtil.parseToken(adminToken)).thenReturn(adminClaims);

            AuthToken newToken = AuthToken.builder()
                .accessToken("admin-new-token")
                .refreshToken("admin-refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(System.currentTimeMillis() / 1000 + 3600)
                .build();

            AuthService.AuthResult result = new AuthService.AuthResult(
                null, newToken, testUser, testBusinessAccount,
                false, null, List.of(testMembership), false
            );
            when(authService.selectBusinessAccount(999, 10)).thenReturn(result);

            Map<String, Integer> request = Map.of("businessAccountId", 10);

            // Act & Assert
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(authService).selectBusinessAccount(999, 10);
        }

        @Test
        @DisplayName("多数の所属がある場合も正常に一覧取得できる")
        void shouldReturnManyMemberships() throws Exception {
            // Arrange: 10件の所属を作成
            List<AuthService.MembershipWithAccount> memberships = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> {
                    UserBusinessAccountMembership membership = new UserBusinessAccountMembership();
                    membership.setId(i);
                    membership.setUserId(1);
                    membership.setBusinessAccountId(i + 100);
                    membership.setRole(21);
                    membership.setIsDefault(i == 0);

                    BusinessAccount account = new BusinessAccount();
                    account.setId(i + 100);
                    account.setCode("BA" + i);
                    account.setCompanyName("Company " + i);
                    account.setIsActive(true);
                    account.setIsHeadquarters(false);

                    return new AuthService.MembershipWithAccount(membership, account);
                })
                .toList();

            when(authService.getUserMemberships(1))
                .thenReturn(new AuthService.MembershipsResult(1, memberships));

            // Act & Assert
            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberships", hasSize(10)));
        }
    }

    // ============================================================
    // レスポンス形式テスト
    // ============================================================

    @Nested
    @DisplayName("レスポンス形式テスト")
    class ResponseFormatTests {

        @Test
        @DisplayName("membershipsレスポンスにはユーザーIDと所属一覧が含まれる")
        void shouldHaveCorrectMembershipsResponseFormat() throws Exception {
            List<AuthService.MembershipWithAccount> memberships = List.of(
                new AuthService.MembershipWithAccount(testMembership, testBusinessAccount)
            );
            when(authService.getUserMemberships(1))
                .thenReturn(new AuthService.MembershipsResult(1, memberships));

            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.memberships").isArray());
        }

        @Test
        @DisplayName("selectAccountレスポンスにはトークンとneedsAccountSelectionが含まれる")
        void shouldHaveCorrectSelectAccountResponseFormat() throws Exception {
            AuthToken newToken = AuthToken.builder()
                .accessToken("test-token")
                .refreshToken("test-refresh")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(System.currentTimeMillis() / 1000 + 3600)
                .build();

            AuthService.AuthResult result = new AuthService.AuthResult(
                null, newToken, testUser, testBusinessAccount,
                false, null, List.of(testMembership), false
            );
            when(authService.selectBusinessAccount(1, 10)).thenReturn(result);

            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"businessAccountId\": 10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.token").exists())
                // JSON field names are in snake_case
                .andExpect(jsonPath("$.token.access_token").exists())
                .andExpect(jsonPath("$.token.refresh_token").exists())
                .andExpect(jsonPath("$.needsAccountSelection").exists());
        }

        @Test
        @DisplayName("レスポンスのContent-Typeはapplication/json")
        void shouldSetCorrectContentType() throws Exception {
            List<AuthService.MembershipWithAccount> memberships = List.of(
                new AuthService.MembershipWithAccount(testMembership, testBusinessAccount)
            );
            when(authService.getUserMemberships(1))
                .thenReturn(new AuthService.MembershipsResult(1, memberships));

            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }
}
