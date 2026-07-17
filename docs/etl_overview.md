# ETL Overview Documentation

## 1. ETL 全体の概要

### 目的
このプロジェクトは、PostgreSQL（上流システム）から顧客・証券・取引データを抽出し、SQL Server（LRMシステム）にロードするETLバッチ処理を実装しています。最終的にTrade_Infoテーブルを作成し、顧客名と証券名を結合した取引情報を提供します。

### 処理対象
- **Customer（顧客マスタ）**: PostgreSQLのupstream_customerテーブルからSQL ServerのCustomerテーブルへ
- **Security（証券マスタ）**: PostgreSQLのupstream_securityテーブルからSQL ServerのSecurityテーブルへ
- **Trade（取引データ）**: PostgreSQLのupstream_tradeテーブルからSQL ServerのTradeテーブルへ（CSV経由）

### 最終成果物
- **Trade_Infoテーブル**: TradeテーブルにCustomerとSecurityテーブルを結合し、顧客名と証券名を付加した統合テーブル

---

## 2. テーブル構造一覧

### 2.1 Customer テーブル（SQL Server）
| カラム名 | データ型 | 最大長 | NULL許容 | 説明 |
|----------|----------|--------|----------|------|
| id | bigint | - | NO | 自動採番ID |
| baseDate | varchar | 255 | YES | 基準日 |
| customerCode | varchar | 255 | YES | 顧客コード |
| customerName | varchar | 255 | YES | 顧客名 |

### 2.2 Security テーブル（SQL Server）
| カラム名 | データ型 | 最大長 | NULL許容 | 説明 |
|----------|----------|--------|----------|------|
| id | bigint | - | NO | 自動採番ID |
| baseDate | varchar | 255 | YES | 基準日 |
| securityCode | varchar | 255 | YES | 証券コード |
| securityName | varchar | 255 | YES | 証券名 |

### 2.3 Trade テーブル（SQL Server）
| カラム名 | データ型 | 最大長 | NULL許容 | 説明 |
|----------|----------|--------|----------|------|
| baseDate | varchar | 20 | NO | 基準日 |
| tradeNo | varchar | 20 | NO | 取引番号 |
| customerCode | varchar | 20 | NO | 顧客コード |
| securityCode | varchar | 20 | NO | 証券コード |
| buySell | varchar | 5 | NO | 売買区分（B:買/S:売） |
| quantity | int | - | NO | 数量 |
| amount | int | - | NO | 金額 |
| contractDate | varchar | 20 | NO | 約定日 |
| settlementDate | varchar | 20 | NO | 受渡日 |

### 2.4 Trade_Info テーブル（SQL Server）
| カラム名 | データ型 | 最大長 | NULL許容 | 説明 |
|----------|----------|--------|----------|------|
| baseDate | varchar | 20 | NO | 基準日 |
| tradeNo | varchar | 20 | NO | 取引番号 |
| customerCode | varchar | 20 | NO | 顧客コード |
| customerName | varchar | 255 | YES | 顧客名（Customer結合） |
| securityCode | varchar | 20 | NO | 証券コード |
| securityName | varchar | 255 | YES | 証券名（Security結合） |
| buySell | varchar | 5 | NO | 売買区分 |
| quantity | int | - | NO | 数量 |
| amount | int | - | NO | 金額 |
| contractDate | varchar | 20 | NO | 約定日 |
| settlementDate | varchar | 20 | NO | 受渡日 |

---

## 3. 各 ETL Step の処理内容

### 3.1 Customer ETL
**フロー**: customerInitStep → customerCsvStep

#### customerInitStep（初期化）
- **役割**: CustomerテーブルのTRUNCATE
- **Tasklet**: CustomerInitTasklet
- **SQL**: `TRUNCATE TABLE Customer`
- **データソース**: SQL Server

#### customerCsvStep（CSV→DB）
- **役割**: PostgreSQLからCustomerデータを読み込み、SQL Serverへ書き込み
- **Reader**: CustomerCsvReader（PostgreSQLのupstream_customerを読み込み）
- **Processor**: CustomerProcessor（customerNameのtrim処理）
- **Writer**: CustomerDbWriter（SQL ServerのCustomerへINSERT）
- **Chunkサイズ**: 100

### 3.2 Security ETL
**フロー**: securityInitStep → securityCsvStep

#### securityInitStep（初期化）
- **役割**: SecurityテーブルのTRUNCATE
- **Tasklet**: SecurityInitTasklet
- **SQL**: `TRUNCATE TABLE Security`
- **データソース**: SQL Server

#### securityCsvStep（CSV→DB）
- **役割**: PostgreSQLからSecurityデータを読み込み、SQL Serverへ書き込み
- **Reader**: SecurityCsvReader（PostgreSQLのupstream_securityを読み込み）
- **Processor**: SecurityProcessor（securityNameのtrim処理）
- **Writer**: SecurityDbWriter（SQL ServerのSecurityへINSERT）
- **Chunkサイズ**: 10

### 3.3 Trade ETL
**フロー**: tradeInitStep → tradeCsvStep → tradeLoadStep

#### tradeInitStep（初期化）
- **役割**: TradeテーブルのTRUNCATE
- **Tasklet**: TradeInitTasklet
- **SQL**: `TRUNCATE TABLE Trade`
- **データソース**: SQL Server

#### tradeCsvStep（PostgreSQL→CSV）
- **役割**: PostgreSQLからTradeデータを読み込み、CSVファイルに出力
- **Tasklet**: TradeCsvTasklet
- **データソース**: PostgreSQL
- **出力先**: `C:/AIDev/Projects/csv-batch2/target/trade.csv`
- **特徴**: `allowStartIfComplete(true)` で前回COMPLETEDでも必ず実行
- **SQL**: 
```sql
SELECT
    base_date, trade_no, customer_code, security_code,
    buy_sell, quantity, amount, contract_date, settlement_date
FROM upstream_trade
ORDER BY trade_no
```

#### tradeLoadStep（CSV→DB）
- **役割**: CSVファイルからTradeデータを読み込み、SQL Serverへ書き込み
- **Tasklet**: TradeLoadTasklet
- **データソース**: SQL Server
- **入力元**: `C:/AIDev/Projects/csv-batch2/target/trade.csv`
- **特徴**: `allowStartIfComplete(true)` で前回COMPLETEDでも必ず実行
- **SQL**: 
```sql
INSERT INTO Trade (
    baseDate, tradeNo, customerCode, securityCode,
    buySell, quantity, amount, contractDate, settlementDate
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
```

---

## 4. 各 Tasklet / Processor / Reader / Writer の役割と処理フロー

### 4.1 Tasklet 一覧

#### CustomerInitTasklet
- **パッケージ**: jp.lrm.batch.tasklet
- **役割**: Customerテーブルの初期化（TRUNCATE）
- **データソース**: SQL Server（@Qualifier("sqlServerDataSource")）
- **SQLファイル**: `classpath:sql/lrm/customer_truncate.sql`
- **処理**: SQLファイルを読み込み、JdbcTemplateで実行

#### SecurityInitTasklet
- **パッケージ**: jp.lrm.batch.tasklet
- **役割**: Securityテーブルの初期化（TRUNCATE）
- **データソース**: SQL Server（@Qualifier("sqlServerDataSource")）
- **SQLファイル**: `classpath:sql/lrm/security_truncate.sql`
- **処理**: SQLファイルを読み込み、JdbcTemplateで実行

#### TradeInitTasklet
- **パッケージ**: jp.lrm.batch.tasklet
- **役割**: Tradeテーブルの初期化（TRUNCATE）
- **データソース**: SQL Server（@Qualifier("sqlServerDataSource")）
- **SQLファイル**: `classpath:sql/lrm/trade_truncate.sql`
- **処理**: SQLファイルを読み込み、JdbcTemplateで実行

#### TradeCsvTasklet
- **パッケージ**: jp.lrm.batch.tasklet
- **役割**: PostgreSQLからTradeデータをCSVに出力
- **データソース**: PostgreSQL（@Qualifier("postgresDataSource")）
- **出力パス**: `${batch.trade.csv.path}`（application.propertiesで設定）
- **処理**: 
  1. upstream_tradeテーブルから全データをSELECT
  2. カンマ区切りでフォーマット
  3. ヘッダ行を追加
  4. CSVファイルに書き込み

#### TradeLoadTasklet
- **パッケージ**: jp.lrm.batch.tasklet
- **役割**: CSVファイルからTradeデータをSQL Serverにロード
- **データソース**: SQL Server（@Qualifier("sqlServerDataSource")）
- **入力パス**: `${batch.trade.csv.path}`（application.propertiesで設定）
- **処理**:
  1. CSVファイルを読み込み
  2. ヘッダ行を削除
  3. 各行をカンマで分割
  4. TradeテーブルにINSERT

### 4.2 Processor 一覧

#### CustomerProcessor
- **パッケージ**: jp.lrm.batch.processor
- **役割**: Customerデータの変換処理
- **処理**: customerNameのtrim処理
- **入出力**: Customer → Customer

#### SecurityProcessor
- **パッケージ**: jp.lrm.batch.processor
- **役割**: Securityデータの変換処理
- **処理**: securityNameのtrim処理
- **入出力**: Security → Security

#### TradeProcessor
- **パッケージ**: jp.lrm.batch.processor
- **役割**: Tradeデータの変換処理
- **処理**: buySellのtrim処理
- **入出力**: Trade → Trade
- **注**: 現在のTrade ETLでは使用されていない（Tasklet方式）

### 4.3 Reader 一覧

#### CustomerCsvReader
- **パッケージ**: jp.lrm.batch.reader
- **役割**: PostgreSQLからCustomerデータを読み込み
- **データソース**: PostgreSQL（postgresDataSource）
- **実装**: JdbcCursorItemReader
- **SQL**:
```sql
SELECT
    base_date AS baseDate,
    customer_code AS customerCode,
    customer_name AS customerName
FROM upstream_customer
```
- **RowMapper**: Customerオブジェクトにマッピング

#### SecurityCsvReader
- **パッケージ**: jp.lrm.batch.reader
- **役割**: PostgreSQLからSecurityデータを読み込み
- **データソース**: PostgreSQL（postgresDataSource）
- **実装**: JdbcCursorItemReader
- **SQL**:
```sql
SELECT
    base_date AS baseDate,
    security_code AS securityCode,
    security_name AS securityName
FROM upstream_security
```
- **RowMapper**: Securityオブジェクトにマッピング

#### TradeCsvReader
- **パッケージ**: jp.lrm.batch.reader
- **役割**: PostgreSQLからTradeデータを読み込み
- **データソース**: PostgreSQL（postgresDataSource）
- **実装**: JdbcCursorItemReader
- **SQLファイル**: `classpath:sql/upstream/trade_select.sql`
- **RowMapper**: Tradeオブジェクトにマッピング
- **注**: 現在のTrade ETLでは使用されていない（Tasklet方式）

### 4.4 Writer 一覧

#### CustomerDbWriter
- **パッケージ**: jp.lrm.batch.writer
- **役割**: CustomerデータをSQL Serverに書き込み
- **データソース**: SQL Server（@Qualifier("sqlServerDataSource")）
- **実装**: JdbcBatchItemWriter
- **SQL**:
```sql
INSERT INTO Customer (baseDate, customerCode, customerName)
VALUES (:baseDate, :customerCode, :customerName)
```
- **マッピング**: BeanMapped（Customerオブジェクトのプロパティをバインド）

#### SecurityDbWriter
- **パッケージ**: jp.lrm.batch.writer
- **役割**: SecurityデータをSQL Serverに書き込み
- **データソース**: SQL Server（@Qualifier("sqlServerDataSource")）
- **実装**: JdbcBatchItemWriter
- **SQL**:
```sql
INSERT INTO Security (baseDate, securityCode, securityName)
VALUES (:baseDate, :securityCode, :securityName)
```
- **マッピング**: BeanMapped（Securityオブジェクトのプロパティをバインド）

#### TradeCsvWriter
- **パッケージ**: jp.lrm.batch.writer
- **役割**: TradeデータをCSVファイルに書き込み
- **実装**: FlatFileItemWriter
- **出力先**: `target/trade.csv`
- **フォーマット**: カンマ区切り
- **ヘッダー**: `baseDate,tradeNo,customerCode,securityCode,buySell,quantity,amount,contractDate,settlementDate`
- **注**: 現在のTrade ETLでは使用されていない（Tasklet方式）

---

## 5. Trade_Info 作成の DDL と説明

### 5.1 DDL（sql/create_trade_info.sql）

```sql
-- Trade_Info Table DDL
-- Based on Trade table with customerName and securityName added

IF OBJECT_ID('dbo.Trade_Info', 'U') IS NOT NULL
    DROP TABLE dbo.Trade_Info;
GO

CREATE TABLE dbo.Trade_Info (
    baseDate VARCHAR(20) NOT NULL,
    tradeNo VARCHAR(20) NOT NULL,
    customerCode VARCHAR(20) NOT NULL,
    customerName VARCHAR(255) NULL,
    securityCode VARCHAR(20) NOT NULL,
    securityName VARCHAR(255) NULL,
    buySell VARCHAR(5) NOT NULL,
    quantity INT NOT NULL,
    amount INT NOT NULL,
    contractDate VARCHAR(20) NOT NULL,
    settlementDate VARCHAR(20) NOT NULL
);
GO
```

### 5.2 説明
- **ベース**: Tradeテーブル構造をベースに作成
- **追加カラム**:
  - `customerName`: customerCodeの右側に追加（Customerテーブル結合用）
  - `securityName`: securityCodeの右側に追加（Securityテーブル結合用）
- **データ型**: Tradeテーブルと同様の型を使用
- **NULL許容**: customerNameとsecurityNameはNULL許容（結合失敗時の対応）

---

## 6. PopulateTradeInfo ストアドの処理内容と JOIN 条件の説明

### 6.1 ストアドプロシージャ（sql/populate_trade_info.sql）

```sql
-- Stored Procedure to populate Trade_Info table
-- Joins Customer, Security, and Trade tables and inserts into Trade_Info

IF OBJECT_ID('dbo.PopulateTradeInfo', 'P') IS NOT NULL
    DROP PROCEDURE dbo.PopulateTradeInfo;
GO

CREATE PROCEDURE dbo.PopulateTradeInfo
AS
BEGIN
    SET NOCOUNT ON;

    -- Clear existing data
    TRUNCATE TABLE dbo.Trade_Info;

    -- Insert data from joined tables
    INSERT INTO dbo.Trade_Info (
        baseDate,
        tradeNo,
        customerCode,
        customerName,
        securityCode,
        securityName,
        buySell,
        quantity,
        amount,
        contractDate,
        settlementDate
    )
    SELECT 
        t.baseDate,
        t.tradeNo,
        t.customerCode,
        c.customerName,
        t.securityCode,
        s.securityName,
        t.buySell,
        t.quantity,
        t.amount,
        t.contractDate,
        t.settlementDate
    FROM dbo.Trade t
    LEFT JOIN dbo.Customer c ON t.customerCode = c.customerCode AND t.baseDate = c.baseDate
    LEFT JOIN dbo.Security s ON t.securityCode = s.securityCode AND t.baseDate = s.baseDate;
END
GO
```

### 6.2 処理内容
1. **既存データの削除**: Trade_InfoテーブルをTRUNCATE
2. **結合クエリの実行**: Trade、Customer、Securityテーブルを結合
3. **データ挿入**: 結合結果をTrade_InfoテーブルにINSERT

### 6.3 JOIN 条件の説明
- **Trade ↔ Customer**: 
  - `t.customerCode = c.customerCode`（顧客コードで結合）
  - `t.baseDate = c.baseDate`（基準日でも結合）
  - **理由**: 同じ顧客コードでも日付によって顧客名が変更される可能性があるため

- **Trade ↔ Security**:
  - `t.securityCode = s.securityCode`（証券コードで結合）
  - `t.baseDate = s.baseDate`（基準日でも結合）
  - **理由**: 同じ証券コードでも日付によって証券名が変更される可能性があるため

- **LEFT JOIN**: 結合失敗時もTradeデータを保持（customerName、securityNameはNULL）

---

## 7. ジョブ全体の処理順序（時系列のフロー図）

### 7.1 メインジョブ（csvBatchJob）

```
┌─────────────────────────────────────────────────────────────────┐
│ csvBatchJob                                                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. customerInitStep                                             │
│     └─ CustomerInitTasklet                                       │
│        └─ TRUNCATE TABLE Customer (SQL Server)                  │
│                                                                  │
│  2. customerCsvStep                                              │
│     ├─ CustomerCsvReader (PostgreSQL)                           │
│     ├─ CustomerProcessor (trim)                                 │
│     └─ CustomerDbWriter (SQL Server)                            │
│                                                                  │
│  3. securityInitStep                                             │
│     └─ SecurityInitTasklet                                      │
│        └─ TRUNCATE TABLE Security (SQL Server)                  │
│                                                                  │
│  4. securityCsvStep                                              │
│     ├─ SecurityCsvReader (PostgreSQL)                           │
│     ├─ SecurityProcessor (trim)                                 │
│     └─ SecurityDbWriter (SQL Server)                            │
│                                                                  │
│  5. tradeInitStep                                                │
│     └─ TradeInitTasklet                                         │
│        └─ TRUNCATE TABLE Trade (SQL Server)                     │
│                                                                  │
│  6. tradeCsvStep                                                 │
│     └─ TradeCsvTasklet                                           │
│        ├─ SELECT FROM upstream_trade (PostgreSQL)              │
│        └─ WRITE TO trade.csv                                    │
│                                                                  │
│  7. tradeLoadStep                                                │
│     └─ TradeLoadTasklet                                          │
│        ├─ READ FROM trade.csv                                   │
│        └─ INSERT INTO Trade (SQL Server)                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 Trade_Info 作成（手動実行）

```
┌─────────────────────────────────────────────────────────────────┐
│ Trade_Info 作成フロー                                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Trade_Info テーブル作成                                      │
│     └─ sql/create_trade_info.sql を実行                         │
│                                                                  │
│  2. PopulateTradeInfo ストアド実行                               │
│     └─ EXEC dbo.PopulateTradeInfo                               │
│        ├─ TRUNCATE TABLE Trade_Info                             │
│        ├─ Trade + Customer + Security を結合                     │
│        └─ INSERT INTO Trade_Info                                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 7.3 データフロー全体

```
PostgreSQL（上流システム）                    SQL Server（LRMシステム）
┌──────────────────────┐                    ┌──────────────────────┐
│ upstream_customer     │                    │ Customer             │
│ upstream_security     │────── ETL ───────▶│ Security             │
│ upstream_trade        │                    │ Trade                │
└──────────────────────┘                    │ Trade_Info           │
                                            └──────────────────────┘
                                                      ▲
                                                      │
                                                 手動実行
                                            (PopulateTradeInfo)
```

---

## 8. GitHub の main ブランチにある SQL ファイルの説明

### 8.1 sql/create_trade_info.sql
- **目的**: Trade_InfoテーブルのDDL
- **内容**: TradeテーブルをベースにcustomerNameとsecurityNameを追加したテーブル定義
- **実行タイミング**: 初回のみ（テーブル作成時）
- **特徴**: 既存テーブルがある場合はDROPして再作成

### 8.2 sql/populate_trade_info.sql
- **目的**: Trade_Infoテーブルにデータを投入するストアドプロシージャ
- **内容**: Trade、Customer、Securityテーブルを結合してTrade_InfoにINSERT
- **実行タイミング**: Tradeデータ更新後
- **特徴**: 
  - TRUNCATEで既存データを削除
  - baseDateを含むJOIN条件で正確な結合
  - LEFT JOINで結合失敗時もTradeデータを保持

---

## 9. データソース設定（application.properties）

### 9.1 PostgreSQL（上流システム）
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=Dev1000
spring.datasource.driver-class-name=org.postgresql.Driver
```
- **用途**: Customer、Security、Tradeデータの読み取り
- **設定**: @Primaryアノテーションでデフォルトデータソースに設定

### 9.2 SQL Server（LRMシステム）
```properties
spring.datasource.mssql.url=jdbc:sqlserver://localhost:1433;databaseName=mini_jpa;encrypt=false
spring.datasource.mssql.username=sa
spring.datasource.mssql.password=Dev1000
spring.datasource.mssql.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```
- **用途**: Customer、Security、Tradeデータの書き込み
- **設定**: @Qualifier("sqlServerDataSource")で明示的に指定

### 9.3 CSV パス設定
```properties
batch.trade.csv.path=C:/AIDev/Projects/csv-batch2/target/trade.csv
```
- **用途**: Trade CSVファイルの入出力パス
- **使用**: TradeCsvTasklet（出力）、TradeLoadTasklet（入力）

---

## 10. 今後の拡張案

### 10.1 自動化の改善
- **Trade_Info 自動更新**: TradeLoadStepの後にPopulateTradeInfoを自動実行するStepを追加
- **スケジュール実行**: Spring Schedulerで定期的にETLを実行
- **エラーハンドリング**: 各Stepでの例外処理とリトライ機構の実装

### 10.2 データ品質の向上
- **バリデーション**: Processorでのデータチェック強化
- **重複排除**: customerCode+baseDate、securityCode+baseDateでの重複チェック
- **データ整合性**: 外部キー制約の追加

### 10.3 パフォーマンス改善
- **バッチサイズ最適化**: Chunkサイズのチューニング
- **並列処理**: Multi-threaded Stepの導入
- **一括INSERT**: バッチ更新の最適化

### 10.4 監視・ログ
- **メトリクス収集**: 処理件数、処理時間の記録
- **アラート**: エラー発生時の通知機能
- **監査ログ**: データ変更履歴の記録

### 10.5 機能追加
- **差分更新**: 全件ロードではなく差分のみを更新
- **履歴管理**: SCD（Slowly Changing Dimension）の実装
- **データマッピング**: 柔軟なマッピング定義機能

---

## 11. まとめ

このETLシステムは、PostgreSQLからSQL Serverへのデータ移行を効率的に行うSpring Batchアプリケーションです。CustomerとSecurityは直接チャンク処理で移行し、TradeはCSVを経由して移行します。最終的にTrade_Infoテーブルを作成することで、顧客名と証券名を結合した取引情報を提供します。

**主要な特徴**:
- 複数データソース（PostgreSQL、SQL Server）の対応
- チャンク処理とタスクレット処理の使い分け
- CSVを介したTradeデータの移行
- baseDateを考慮した正確なデータ結合
- 柔軟な拡張性を持つアーキテクチャ
