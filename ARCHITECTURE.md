# CSV Batch 2 - Architecture Diagram

## Overview
Spring Batch application that transfers data between PostgreSQL (upstream) and SQL Server (LRM) with CSV export capabilities. The system creates a unified Trade_Info table by joining Trade data with Customer and Security master data.

## Technology Stack
- **Framework**: Spring Boot 3.3.2 + Spring Batch
- **Java**: 17
- **Databases**: PostgreSQL (source), SQL Server (target)
- **CSV Library**: OpenCSV 5.9
- **Build**: Maven

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Spring Batch Application                            │
│                           (CsvBatch2Application)                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┴─────────────────┐
                    │                                     │
        ┌───────────▼───────────┐           ┌───────────▼───────────┐
        │  PostgreSQL DataSource │           │  SQL Server DataSource │
        │  (Primary)             │           │  (LRM)                  │
        │  - postgresDataSource  │           │  - sqlServerDataSource  │
        │  - postgresTxManager   │           │  - sqlServerTxManager   │
        └───────────┬───────────┘           └───────────┬───────────┘
                    │                                     │
        ┌───────────▼───────────┐           ┌───────────▼───────────┐
        │   upstream_customer   │           │   Customer Table       │
        │   upstream_security   │           │   Security Table       │
        │   upstream_trade      │           │   Trade Table          │
        └───────────────────────┘           │   Trade_Info Table     │
                                          └───────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           MAIN JOB: csvBatchJob                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. customerInitStep                                                        │
│     └─ CustomerInitTasklet                                                  │
│        └─ TRUNCATE TABLE Customer (SQL Server)                             │
│                                                                             │
│  2. customerCsvStep                                                         │
│     ├─ CustomerCsvReader (PostgreSQL)                                        │
│     ├─ CustomerProcessor (trim)                                            │
│     └─ CustomerDbWriter (SQL Server)                                         │
│                                                                             │
│  3. securityInitStep                                                        │
│     └─ SecurityInitTasklet                                                  │
│        └─ TRUNCATE TABLE Security (SQL Server)                              │
│                                                                             │
│  4. securityCsvStep                                                         │
│     ├─ SecurityCsvReader (PostgreSQL)                                       │
│     ├─ SecurityProcessor (trim)                                            │
│     └─ SecurityDbWriter (SQL Server)                                       │
│                                                                             │
│  5. tradeInitStep                                                           │
│     └─ TradeInitTasklet                                                     │
│        └─ TRUNCATE TABLE Trade (SQL Server)                                 │
│                                                                             │
│  6. tradeCsvStep                                                            │
│     └─ TradeCsvTasklet                                                      │
│        ├─ SELECT FROM upstream_trade (PostgreSQL)                          │
│        └─ WRITE TO trade.csv                                                │
│                                                                             │
│  7. tradeLoadStep                                                           │
│     └─ TradeLoadTasklet                                                     │
│        ├─ READ FROM trade.csv                                               │
│        └─ INSERT INTO Trade (SQL Server)                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      Trade_Info Creation (Manual Execution)                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. Create Trade_Info Table                                                 │
│     └─ sql/create_trade_info.sql                                           │
│        └─ Based on Trade + customerName + securityName                      │
│                                                                             │
│  2. Populate Trade_Info                                                    │
│     └─ EXEC dbo.PopulateTradeInfo                                           │
│        ├─ TRUNCATE TABLE Trade_Info                                         │
│        ├─ Trade + Customer + Security JOIN (with baseDate)                  │
│        │   - Trade ↔ Customer: customerCode + baseDate                     │
│        │   - Trade ↔ Security: securityCode + baseDate                     │
│        └─ INSERT INTO Trade_Info                                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

## Package Structure

```
jp.lrm.batch/
├── CsvBatch2Application.java          # Main Spring Boot application
├── config/
│   ├── PostgresDataSourceConfig.java  # PostgreSQL datasource (primary)
│   └── SqlServerDataSourceConfig.java # SQL Server datasource (LRM)
├── job/
│   └── CsvBatchJobConfig.java         # Main ETL job (all steps)
├── model/
│   ├── Customer.java                  # Customer entity
│   ├── Security.java                  # Security entity
│   └── Trade.java                     # Trade entity
├── processor/
│   ├── CustomerProcessor.java         # Customer data transformation (trim)
│   └── SecurityProcessor.java         # Security data transformation (trim)
├── reader/
│   ├── CustomerCsvReaderConfig.java   # Reads from PostgreSQL
│   └── SecurityCsvReaderConfig.java   # Reads from PostgreSQL
├── step/
│   ├── CustomerCsvStepConfig.java     # Customer chunk step
│   ├── CustomerInitStepConfig.java    # Customer init step
│   ├── SecurityCsvStepConfig.java     # Security chunk step
│   ├── SecurityInitStepConfig.java    # Security init step
│   ├── TradeCsvStepConfig.java        # Trade CSV step
│   ├── TradeInitStepConfig.java       # Trade init step
│   └── TradeLoadStepConfig.java       # Trade load step
├── tasklet/
│   ├── CustomerInitTasklet.java       # TRUNCATE Customer table
│   ├── SecurityInitTasklet.java       # TRUNCATE Security table
│   ├── TradeCsvTasklet.java           # Export PostgreSQL to CSV
│   ├── TradeInitTasklet.java          # TRUNCATE Trade table
│   └── TradeLoadTasklet.java          # Load CSV to SQL Server
└── writer/
    ├── CustomerDbWriterConfig.java    # Write to SQL Server
    └── SecurityDbWriterConfig.java    # Write to SQL Server
```

## Data Flow

### Customer & Security (Direct DB-to-DB Transfer)
```
PostgreSQL (upstream_customer/upstream_security)
    ↓ [JdbcCursorItemReader]
Customer/Security Processor (trim)
    ↓ [JdbcBatchItemWriter]
SQL Server (Customer/Security tables)
```

### Trade (PostgreSQL → CSV → SQL Server)
```
PostgreSQL (upstream_trade)
    ↓ [TradeCsvTasklet - SELECT query]
CSV File (target/trade.csv)
    ↓ [TradeLoadTasklet - parse CSV]
SQL Server (Trade table)
```

### Trade_Info Creation (Manual)
```
SQL Server (Trade + Customer + Security)
    ↓ [PopulateTradeInfo stored procedure]
    - JOIN: Trade ↔ Customer (customerCode + baseDate)
    - JOIN: Trade ↔ Security (securityCode + baseDate)
SQL Server (Trade_Info table)
```

## Configuration

### Application Properties
- **PostgreSQL**: localhost:5432/postgres (primary datasource)
- **SQL Server**: localhost:1433/mini_jpa (LRM datasource)
- **Batch Metadata**: Auto-initialize schema
- **Logging**: Spring Batch INFO level

### SQL Scripts
- `src/main/resources/sql/lrm/customer_truncate.sql` - TRUNCATE Customer table
- `src/main/resources/sql/lrm/security_truncate.sql` - TRUNCATE Security table
- `src/main/resources/sql/lrm/trade_truncate.sql` - TRUNCATE Trade table
- `sql/create_trade_info.sql` - Create Trade_Info table with customerName and securityName
- `sql/populate_trade_info.sql` - Stored procedure to populate Trade_Info with JOIN conditions

## Key Design Patterns

1. **Dual DataSource Configuration**: Separate datasources for PostgreSQL (primary) and SQL Server with dedicated transaction managers
2. **Chunk-oriented Processing**: Customer and Security jobs use chunk processing (100 items/chunk for Customer, 10 items/chunk for Security) for performance
3. **Tasklet-based Processing**: Trade job uses tasklets for CSV export/import operations
4. **Bean Mapping**: Writers use beanMapped() for automatic parameter binding
5. **baseDate-based JOIN**: Trade_Info creation uses baseDate in JOIN conditions to handle temporal data changes
6. **Unified Job Flow**: Single csvBatchJob orchestrates all ETL steps in sequence
