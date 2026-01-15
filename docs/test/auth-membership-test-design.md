# マルチビジネスアカウント所属機能 テスト設計書

## 1. 概要

本ドキュメントは、マルチビジネスアカウント所属機能のサーバーサイドテストの設計について記述する。

### 1.1 テスト対象機能

- ユーザーの所属ビジネスアカウント一覧取得 (`GET /apix/Auth/memberships`)
- ビジネスアカウント選択/切り替え (`POST /apix/Auth/selectAccount`)
- 認証フロー（既存機能との統合）

### 1.2 テスト環境

| 項目 | 値 |
|------|-----|
| フレームワーク | JUnit 5 |
| テストランナー | Spring Boot Test |
| モックライブラリ | Mockito |
| データベース | H2 (in-memory, MySQL互換モード) |
| Spring Boot | 3.5.9 |
| Java | 24 |

---

## 2. テストファイル構成

```
src/test/java/com/nines/nutsfact/
├── api/v1/controller/
│   └── AuthControllerMembershipTest.java    # コントローラー統合テスト
├── domain/service/
│   └── AuthServiceMembershipTest.java       # サービス層ユニットテスト
└── security/
    └── SqlInjectionSecurityTest.java        # セキュリティテスト
```

---

## 3. テストクラス詳細

### 3.1 AuthControllerMembershipTest

**目的**: コントローラー層の統合テスト。HTTP リクエスト/レスポンスの検証。

**アノテーション**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
```

**モック対象**:
- `AuthService` - ビジネスロジック
- `JwtUtil` - JWT検証

#### テストケース一覧

| カテゴリ | テストケース | 検証内容 |
|----------|--------------|----------|
| **GET /memberships** | | |
| 正常系 | shouldReturnMembershipsForAuthenticatedUser | 認証済みユーザーが所属一覧を取得 |
| 正常系 | shouldReturnEmptyArrayWhenNoMemberships | 所属がない場合は空配列 |
| 正常系 | shouldReturnSingleMembership | 単一所属の詳細取得 |
| 異常系 | shouldReturn403WhenNotAuthenticated | 認証なしで403 |
| 異常系 | shouldReturn403WithInvalidToken | 無効トークンで403 |
| **POST /selectAccount** | | |
| 正常系 | shouldSelectValidBusinessAccount | 有効なアカウント選択 |
| 異常系 | shouldReturnErrorWhenBusinessAccountIdIsNull | null値で500 |
| 異常系 | shouldReturn401WhenSelectingUnaffiliatedAccount | 非所属アカウントで401 |
| 異常系 | shouldReturn401WhenSelectingInactiveAccount | 無効化アカウントで401 |
| 異常系 | shouldReturn403WhenNotAuthenticated | 認証なしで403 |
| **バリデーション** | | |
| | shouldHandleNegativeNumberInBusinessAccountId | 負数処理 |
| | shouldHandleZeroBusinessAccountId | ゼロ処理 |
| | shouldReturnErrorForEmptyBody | 空ボディ処理 |
| **エッジケース** | | |
| | shouldHandleReselectingSameAccount | 同一アカウント再選択 |
| | shouldAllowSystemAdminToSelectAccount | 運営管理者の選択 |
| | shouldReturnManyMemberships | 多数所属の取得 |
| **レスポンス形式** | | |
| | shouldHaveCorrectMembershipsResponseFormat | membershipsレスポンス形式 |
| | shouldHaveCorrectSelectAccountResponseFormat | selectAccountレスポンス形式 |
| | shouldSetCorrectContentType | Content-Type検証 |

---

### 3.2 AuthServiceMembershipTest

**目的**: サービス層のユニットテスト。ビジネスロジックの検証。

**アノテーション**:
```java
@ExtendWith(MockitoExtension.class)
```

**モック対象**:
- `UserRepository`
- `BusinessAccountRepository`
- `BusinessAccountService` - ユーザー数を動的に計算するサービス
- `UserBusinessAccountMembershipRepository`
- `JwtUtil`
- `PasswordEncoder`
- `SystemParameterService`
- `GoogleIdTokenVerifierService`

#### テストケース一覧

| カテゴリ | テストケース | 検証内容 |
|----------|--------------|----------|
| **selectBusinessAccount** | | |
| 正常系 | shouldSelectValidBusinessAccount | 正常なアカウント選択 |
| 異常系 | shouldThrowWhenUserNotAffiliated | 非所属時の例外 |
| 異常系 | shouldThrowWhenBusinessAccountInactive | 無効アカウント時の例外 |
| **getUserMemberships** | | |
| 正常系 | shouldReturnAllMemberships | 全所属取得 |
| 正常系 | shouldReturnEmptyForUserWithNoMemberships | 所属なしユーザー |
| **signIn (マルチアカウント)** | | |
| | shouldSetNeedsAccountSelectionForMultipleMemberships | 複数所属時のフラグ |
| | shouldNotSetNeedsAccountSelectionForSingleMembership | 単一所属時のフラグ |

---

### 3.3 SqlInjectionSecurityTest

**目的**: セキュリティ脆弱性テスト。攻撃耐性の検証。

**アノテーション**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
```

#### テストケース一覧

| カテゴリ | テストケース | 検証内容 |
|----------|--------------|----------|
| **SQLインジェクション** | | |
| | shouldRejectSqlInjectionPayloads (11パターン) | SQL攻撃ペイロード拒否 |
| | shouldOnlyAcceptIntegerValues | 文字列拒否 |
| | shouldHandleDecimalValues | 小数処理 |
| | shouldRejectArrayValues | 配列拒否 |
| | shouldRejectObjectValues | オブジェクト拒否 |
| | shouldHandleVeryLargeNumbers | 大数値処理 |
| | shouldHandleVeryLargeNegativeNumbers | 負の大数値処理 |
| **情報漏洩防止** | | |
| | shouldNotLeakStackTrace | スタックトレース非公開 |
| | shouldNotLeakDatabaseInfo | DB情報非公開 |
| | shouldNotLeakTableNames | テーブル名非公開 |
| **認証バイパス** | | |
| | shouldRequireAuthentication | 認証必須 |
| | shouldRejectTamperedToken | 改ざんトークン拒否 |
| | shouldRejectEmptyAuthHeader | 空ヘッダー拒否 |
| | shouldRejectNonBearerScheme | 非Bearerスキーム拒否 |
| | shouldHandleMaliciousJwtPayload | 悪意あるペイロード処理 |
| **DoS防止** | | |
| | shouldHandleVeryLongRequests | 長大リクエスト処理 |
| | shouldHandleDeeplyNestedJson | 深ネストJSON処理 |
| | shouldHandleInvalidContentType | 不正Content-Type処理 |
| **XSS防止** | | |
| | shouldSetJsonContentType | JSONコンテンツタイプ設定 |
| | shouldSanitizeHtmlTags | HTMLタグ無害化 |

---

## 4. SQLインジェクションテストパターン

以下のペイロードをテストで使用:

```
1; DROP TABLE users;--
1' OR '1'='1
1' OR '1'='1'--
1; SELECT * FROM users;--
1' UNION SELECT * FROM users--
1'; DELETE FROM users WHERE '1'='1
1' AND 1=1--
1' AND 1=0--
-1' OR 1=1#
1'/**/OR/**/1=1--
1%27%20OR%201%3D1--  (URLエンコード)
```

---

## 5. 設定ファイル

### 5.1 application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver

mybatis:
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath:mapper/**/*.xml

jwt:
  secret: test-secret-key-for-unit-tests-minimum-32-chars
  access-token-expiration: 3600
  refresh-token-expiration: 604800

logging:
  level:
    root: WARN
    com.nines.nutsfact: DEBUG
    org.springframework.security: DEBUG
```

---

## 6. 依存関係 (build.gradle)

```groovy
// Testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.security:spring-security-test'
testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.5'
testImplementation 'com.h2database:h2'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

---

## 7. 注意事項

### 7.1 Spring Boot 3.5.9 対応

- `@MockBean` は非推奨。`@MockitoBean` を使用すること。
- インポート: `org.springframework.test.context.bean.override.mockito.MockitoBean`

### 7.2 Spring Security のレスポンスコード

- 認証なしリクエスト: **403 Forbidden** (401ではない)
- 無効トークン: **403 Forbidden**
- 認証済み + ビジネスロジックエラー: **401 Unauthorized**

### 7.3 JSON フィールド名

エンティティのフィールド名は snake_case でシリアライズされる:

| Java フィールド | JSON フィールド |
|----------------|-----------------|
| accessToken | access_token |
| refreshToken | refresh_token |
| businessAccountId | business_account_id |
| companyName | company_name |

### 7.4 jjwt 0.12.x API

Claims生成には `Jwts.claims()` ビルダーを使用:

```java
Claims claims = Jwts.claims()
    .subject("1")
    .add("email", "test@example.com")
    .add("businessAccountId", 10)
    .add("role", 21)
    .add("type", "access")
    .build();
```

---

## 8. 改訂履歴

| 日付 | バージョン | 変更内容 |
|------|-----------|----------|
| 2026-01-16 | 1.0 | 初版作成 |
| 2026-01-16 | 1.1 | BusinessAccountServiceのモック追加（ユーザー数動的計算対応） |
