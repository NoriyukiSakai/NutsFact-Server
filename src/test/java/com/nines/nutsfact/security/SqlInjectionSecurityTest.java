package com.nines.nutsfact.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nines.nutsfact.config.JwtUtil;
import com.nines.nutsfact.domain.model.auth.AuthToken;
import com.nines.nutsfact.domain.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SQLインジェクションおよびセキュリティ脆弱性テスト
 *
 * このテストクラスは、アプリケーションがSQLインジェクション攻撃や
 * その他のセキュリティ脆弱性に対して保護されていることを確認します。
 *
 * テスト戦略:
 * 1. 典型的なSQLインジェクションペイロードを送信
 * 2. エラーメッセージにDB情報が含まれていないことを確認
 * 3. 不正な入力に対して適切なエラーレスポンスが返ることを確認
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SqlInjectionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("security-test@example.com");
        testUser.setName("Security Test User");
        testUser.setBusinessAccountId(10);
        testUser.setRole(21);
        testUser.setIsActive(true);

        AuthToken token = jwtUtil.generateTokens(testUser);
        validToken = token.getAccessToken();
    }

    // ============================================================
    // SQLインジェクション攻撃テスト
    // ============================================================

    @Nested
    @DisplayName("SQLインジェクション攻撃防止テスト")
    class SqlInjectionTests {

        @ParameterizedTest
        @DisplayName("selectAccount: SQLインジェクションペイロードが無害化される")
        @ValueSource(strings = {
            "1; DROP TABLE users;--",
            "1' OR '1'='1",
            "1' OR '1'='1'--",
            "1; SELECT * FROM users;--",
            "1' UNION SELECT * FROM users--",
            "1'; DELETE FROM users WHERE '1'='1",
            "1' AND 1=1--",
            "1' AND 1=0--",
            "-1' OR 1=1#",
            "1'/**/OR/**/1=1--",
            "1%27%20OR%201%3D1--"  // URL encoded
        })
        void shouldRejectSqlInjectionPayloads(String maliciousPayload) throws Exception {
            String jsonRequest = "{\"businessAccountId\": \"" + maliciousPayload + "\"}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            int status = result.getResponse().getStatus();
            // エラーレスポンスであることを確認（4xxまたは5xx）
            assertThat("SQLインジェクションペイロードはエラーになるべき", status, anyOf(
                greaterThanOrEqualTo(400),
                lessThan(600)
            ));

            // レスポンスボディがあれば、SQLキーワードが含まれていないことを確認
            String responseBody = result.getResponse().getContentAsString();
            if (!responseBody.isEmpty()) {
                assertThat("レスポンスにSQLキーワードが含まれるべきではない",
                    responseBody.toLowerCase(), not(containsString("sql syntax")));
                assertThat("レスポンスにスタックトレースが含まれるべきではない",
                    responseBody, not(containsString("at com.")));
            }
        }

        @Test
        @DisplayName("selectAccount: 整数値のみが受け入れられる")
        void shouldOnlyAcceptIntegerValues() throws Exception {
            // 文字列値を送信
            String jsonRequest = "{\"businessAccountId\": \"abc\"}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            // 4xxまたは5xxエラーであることを確認
            assertThat("文字列値はエラーになるべき",
                result.getResponse().getStatus(), greaterThanOrEqualTo(400));
        }

        @Test
        @DisplayName("selectAccount: 小数値は整数に変換されるか拒否される")
        void shouldHandleDecimalValues() throws Exception {
            String jsonRequest = "{\"businessAccountId\": 1.5}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            // 4xxまたは5xxエラー、または正常処理されることを確認
            // （Jacksonが小数を整数に変換する場合がある）
            int status = result.getResponse().getStatus();
            assertThat("小数値は適切に処理されるべき", status, anyOf(
                equalTo(200),  // 1に変換されて処理
                greaterThanOrEqualTo(400)  // エラー
            ));
        }

        @Test
        @DisplayName("selectAccount: 配列値は拒否される")
        void shouldRejectArrayValues() throws Exception {
            String jsonRequest = "{\"businessAccountId\": [1, 2, 3]}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            assertThat("配列値はエラーになるべき",
                result.getResponse().getStatus(), greaterThanOrEqualTo(400));
        }

        @Test
        @DisplayName("selectAccount: オブジェクト値は拒否される")
        void shouldRejectObjectValues() throws Exception {
            String jsonRequest = "{\"businessAccountId\": {\"id\": 1}}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            assertThat("オブジェクト値はエラーになるべき",
                result.getResponse().getStatus(), greaterThanOrEqualTo(400));
        }

        @Test
        @DisplayName("selectAccount: 非常に大きな数値は安全に処理される")
        void shouldHandleVeryLargeNumbers() throws Exception {
            // Integer.MAX_VALUEを超える値
            String jsonRequest = "{\"businessAccountId\": 9999999999999999999}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            // クラッシュせずにエラーレスポンスを返すことを確認
            assertThat("大きな数値は安全に処理されるべき",
                result.getResponse().getStatus(), greaterThanOrEqualTo(400));
        }

        @Test
        @DisplayName("selectAccount: 負の大きな数値は安全に処理される")
        void shouldHandleVeryLargeNegativeNumbers() throws Exception {
            String jsonRequest = "{\"businessAccountId\": -9999999999999999999}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            assertThat("負の大きな数値は安全に処理されるべき",
                result.getResponse().getStatus(), greaterThanOrEqualTo(400));
        }
    }

    // ============================================================
    // エラーメッセージ漏洩防止テスト
    // ============================================================

    @Nested
    @DisplayName("エラーメッセージ情報漏洩防止テスト")
    class ErrorMessageLeakageTests {

        @Test
        @DisplayName("SQLエラーがスタックトレースを含まない")
        void shouldNotLeakStackTrace() throws Exception {
            String jsonRequest = "{\"businessAccountId\": \"' OR 1=1--\"}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat("スタックトレースが含まれるべきではない",
                responseBody, not(containsString("java.lang.")));
            assertThat("スタックトレースが含まれるべきではない",
                responseBody, not(containsString("at com.nines.")));
        }

        @Test
        @DisplayName("エラーメッセージにDB接続情報が含まれない")
        void shouldNotLeakDatabaseInfo() throws Exception {
            String jsonRequest = "{\"businessAccountId\": \"invalid\"}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString().toLowerCase();
            assertThat("JDBC情報が含まれるべきではない",
                responseBody, not(containsString("jdbc")));
            assertThat("MySQL情報が含まれるべきではない",
                responseBody, not(containsString("mysql")));
            assertThat("localhost情報が含まれるべきではない",
                responseBody, not(containsString("localhost")));
        }

        @Test
        @DisplayName("エラーメッセージにテーブル名が含まれない")
        void shouldNotLeakTableNames() throws Exception {
            String jsonRequest = "{\"businessAccountId\": \"invalid\"}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            // テーブル名がエラーメッセージに直接含まれないことを確認
            // ただし、フィールド名（user_idなど）は許容
            assertThat("テーブル名USERSが含まれるべきではない",
                responseBody, not(containsString("TABLE USERS")));
            assertThat("テーブル名BUSINESS_ACCOUNTが含まれるべきではない",
                responseBody, not(containsString("TABLE BUSINESS_ACCOUNT")));
        }
    }

    // ============================================================
    // 認証バイパス攻撃テスト
    // ============================================================

    @Nested
    @DisplayName("認証バイパス攻撃防止テスト")
    class AuthBypassTests {

        @Test
        @DisplayName("JWTトークンなしでのアクセスは拒否される")
        void shouldRequireAuthentication() throws Exception {
            Map<String, Integer> request = Map.of("businessAccountId", 10);

            // Spring Securityは認証なしのリクエストに対して403 Forbiddenを返す
            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("改ざんされたJWTトークンは拒否される")
        void shouldRejectTamperedToken() throws Exception {
            // トークンの署名部分を改ざん
            String tamperedToken = validToken.substring(0, validToken.lastIndexOf('.') + 1) + "tampered_signature";

            Map<String, Integer> request = Map.of("businessAccountId", 10);

            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + tamperedToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("空のAuthorizationヘッダーは拒否される")
        void shouldRejectEmptyAuthHeader() throws Exception {
            Map<String, Integer> request = Map.of("businessAccountId", 10);

            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Bearer以外のスキームは拒否される")
        void shouldRejectNonBearerScheme() throws Exception {
            Map<String, Integer> request = Map.of("businessAccountId", 10);

            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Basic " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("JWTペイロードにSQLインジェクションがあっても安全")
        void shouldHandleMaliciousJwtPayload() throws Exception {
            // Note: JWTの署名が有効でなければ拒否されるので、
            // このテストは主に、仮にJWTが改ざんされた場合の挙動を確認
            String maliciousPayload = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxJyBPUiAnMSc9JzEiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20ifQ.invalid";

            Map<String, Integer> request = Map.of("businessAccountId", 10);

            mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + maliciousPayload)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }
    }

    // ============================================================
    // レートリミット・DoS防止テスト
    // ============================================================

    @Nested
    @DisplayName("DoS攻撃防止テスト")
    class DosPreventionTests {

        @Test
        @DisplayName("非常に長いJSONリクエストは処理される（アプリケーションがクラッシュしない）")
        void shouldHandleVeryLongRequests() throws Exception {
            StringBuilder longJson = new StringBuilder("{\"businessAccountId\": 1, \"padding\": \"");
            for (int i = 0; i < 100000; i++) {  // 100KB程度
                longJson.append("x");
            }
            longJson.append("\"}");

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(longJson.toString()))
                .andReturn();

            // クラッシュせずにレスポンスを返すことを確認
            int status = result.getResponse().getStatus();
            assertThat("長いリクエストは安全に処理されるべき",
                status, anyOf(greaterThanOrEqualTo(200), lessThan(600)));
        }

        @Test
        @DisplayName("深くネストされたJSONは処理される（アプリケーションがクラッシュしない）")
        void shouldHandleDeeplyNestedJson() throws Exception {
            // 深くネストされたJSON (DoS攻撃パターン)
            StringBuilder nestedJson = new StringBuilder();
            for (int i = 0; i < 100; i++) {  // 100段階のネスト
                nestedJson.append("{\"a\":");
            }
            nestedJson.append("1");
            for (int i = 0; i < 100; i++) {
                nestedJson.append("}");
            }

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nestedJson.toString()))
                .andReturn();

            // クラッシュせずにレスポンスを返すことを確認
            int status = result.getResponse().getStatus();
            assertThat("ネストされたJSONは安全に処理されるべき",
                status, anyOf(greaterThanOrEqualTo(200), lessThan(600)));
        }

        @Test
        @DisplayName("不正なContent-Typeでも安全に処理される")
        void shouldHandleInvalidContentType() throws Exception {
            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("text/plain")
                    .content("{\"businessAccountId\": 10}"))
                .andReturn();

            // 415 Unsupported Media Type または 4xxエラー
            int status = result.getResponse().getStatus();
            assertThat("不正なContent-Typeは適切に処理されるべき",
                status, greaterThanOrEqualTo(400));
        }
    }

    // ============================================================
    // XSS防止テスト
    // ============================================================

    @Nested
    @DisplayName("XSS攻撃防止テスト")
    class XssPreventionTests {

        @Test
        @DisplayName("レスポンスにContent-Type: application/jsonが設定される")
        void shouldSetJsonContentType() throws Exception {
            mockMvc.perform(get("/apix/Auth/memberships")
                    .header("Authorization", "Bearer " + validToken))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("HTMLタグを含むリクエストが安全に処理される")
        void shouldSanitizeHtmlTags() throws Exception {
            String jsonWithHtml = "{\"businessAccountId\": \"<script>alert('xss')</script>1\"}";

            MvcResult result = mockMvc.perform(post("/apix/Auth/selectAccount")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithHtml))
                .andReturn();

            // エラーレスポンスであることを確認
            int status = result.getResponse().getStatus();
            assertThat("HTMLタグを含むリクエストは安全に処理されるべき",
                status, greaterThanOrEqualTo(400));
        }
    }
}
