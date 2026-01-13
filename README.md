# NutsFact Server

NUTSFACT - 食品栄養成分管理システム（リファクタリング版）

## 技術スタック

- Java 24
- Spring Boot 3.5.9
- MyBatis 3.0.5
- MySQL
- Gradle 8.14

## プロジェクト構成

```
src/main/java/com/nines/nutsfact/
├── api/v1/
│   ├── controller/     # REST APIコントローラ
│   ├── request/        # リクエストDTO
│   └── response/       # レスポンスDTO
├── config/             # 設定クラス
├── domain/
│   ├── model/          # ドメインモデル
│   │   └── nutrition/  # 栄養成分（分離）
│   ├── repository/     # リポジトリ層
│   └── service/        # サービス層
├── exception/          # 例外クラス
│   └── handler/        # グローバル例外ハンドラ
└── infrastructure/
    ├── converter/      # オブジェクト変換
    └── mapper/         # MyBatis Mapper
```

## API エンドポイント

### 原材料 API

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/food-raw-materials | 一覧取得 |
| GET | /api/v1/food-raw-materials/{id} | 詳細取得 |
| GET | /api/v1/food-raw-materials/category/{categoryId} | カテゴリ別一覧 |
| POST | /api/v1/food-raw-materials | 新規作成 |
| PUT | /api/v1/food-raw-materials/{id} | 更新 |
| DELETE | /api/v1/food-raw-materials/{id} | 削除 |

### 仕込品 API

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/food-pre-products | 一覧取得 |
| GET | /api/v1/food-pre-products/{id} | 詳細取得（明細含む） |
| GET | /api/v1/food-pre-products/kind/{preKind} | 区分別一覧 |
| POST | /api/v1/food-pre-products | 新規作成 |
| PUT | /api/v1/food-pre-products/{id} | 更新 |
| DELETE | /api/v1/food-pre-products/{id} | 削除 |

### 仕込品明細 API

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/food-pre-product-details/by-pre-id/{preId} | 仕込品IDで一覧取得 |
| GET | /api/v1/food-pre-product-details/{id} | 詳細取得 |
| POST | /api/v1/food-pre-product-details | 新規作成 |
| PUT | /api/v1/food-pre-product-details/{id} | 更新 |
| DELETE | /api/v1/food-pre-product-details/{id} | 削除 |

## セットアップ

### 必要条件

- JDK 24+
- MySQL 8.0+
- Gradle 8.14+

### データベース設定

```sql
CREATE DATABASE NUTSFACT;
CREATE USER 'nutsfact_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL ON NUTSFACT.* TO 'nutsfact_user'@'localhost';
```

### 環境変数（オプション）

```bash
export DB_URL=jdbc:mysql://localhost:3306/NUTSFACT
export DB_USERNAME=nutsfact_user
export DB_PASSWORD=your_password
export SERVER_PORT=8081
export SPRING_PROFILES_ACTIVE=develop
```

### ビルド・実行

```bash
# ビルド
./gradlew build

# 実行
./gradlew bootRun

# テスト
./gradlew test
```

## VSCode での開発

推奨拡張機能:
- Extension Pack for Java
- Spring Boot Extension Pack
- Gradle for Java
- XML (Red Hat)

## 改善点（旧版からの変更）

1. **RESTful API設計** - `/api/v1/` プレフィックスとリソース名のケバブケース化
2. **栄養成分の分離** - 100+フィールドを `NutritionBasic`, `NutritionMinerals`, `NutritionVitamins` に分割
3. **グローバル例外ハンドリング** - `GlobalExceptionHandler` による統一的なエラーレスポンス
4. **Converter層の導入** - Request⇔Entity変換の共通化
5. **Service層へのビジネスロジック集約** - Controller層の簡素化
6. **環境変数対応** - 設定の外部化
