# CSV Batch 2 - Architecture Diagram

## Overview
Spring Batch application that transfers data between PostgreSQL (upstream) and SQL Server (LRM) with CSV export capabilities.

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
        └───────────────────────┘           └───────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              JOB 1: CustomerCsvJob                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────────────┐      ┌──────────────────────────────────────┐  │
│  │ Step 1: customerInit  │ ───► │ Step 2: customerCsvStep              │  │
│  │ (Tasklet)             │      │ (Chunk-oriented: 100 items/chunk)   │  │
│  │                       │      │                                      │  │
│  │ TRUNCATE Customer     │      │ ┌──────────┐  ┌──────────┐  ┌──────┐│  │
│  │ table in SQL Server   │      │ │ Reader   │  │Processor │  │Writer││  │
│  └──────────────────────┘      │ │(Postgres)│→ │(trim)    │→ │(SQL  ││  │
│                                 └──────────┘  └──────────┘  │Server)││  │
│                                                            └──────┘│  │
│  ┌──────────────────────┐      ┌──────────────────────────────────────┘  │
│  │ CustomerInitTasklet  │      │ CustomerCsvReaderConfig                 │
│  └──────────────────────┘      │ CustomerProcessor                        │
│                                 │ CustomerDbWriterConfig                   │
│                                 │ CustomerCsvStepConfig                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              JOB 2: SecurityCsvJob                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────────────┐      ┌──────────────────────────────────────┐  │
│  │ Step 1: securityInit │ ───► │ Step 2: securityCsvStep              │  │
│  │ (Tasklet)             │      │ (Chunk-oriented: 100 items/chunk)   │  │
│  │                       │      │                                      │  │
│  │ TRUNCATE Security     │      │ ┌──────────┐  ┌──────────┐  ┌──────┐│  │
│  │ table in SQL Server   │      │ │ Reader   │  │Processor │  │Writer││  │
│  └──────────────────────┘      │ │(Postgres)│→ │(trim)    │→ │(SQL  ││  │
│                                 └──────────┘  └──────────┘  │Server)││  │
│  ┌──────────────────────┐      └──────────────────────────────────────┘  │
│  │ SecurityInitTasklet  │      │ SecurityCsvReaderConfig                  │
│  └──────────────────────┘      │ SecurityProcessor                         │
│                                 │ SecurityDbWriterConfig                    │
│                                 │ SecurityCsvStepConfig                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              JOB 3: TradeCsvJob                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────────────┐                                                   │
│  │ Step 1: tradeInit    │                                                   │
│  │ (Tasklet)             │                                                   │
│  │                       │                                                   │
│  │ TRUNCATE Trade       │                                                   │
│  │ table in SQL Server   │                                                   │
│  └──────────┬───────────┘                                                   │
│             │                                                               │
│             ▼                                                               │
│  ┌──────────────────────┐                                                   │
│  │ Step 2: tradeCsv     │                                                   │
│  │ (Tasklet)             │                                                   │
│  │                       │                                                   │
│  │ PostgreSQL → CSV      │                                                   │
│  │ (target/trade.csv)    │                                                   │
│  └──────────┬───────────┘                                                   │
│             │                                                               │
│             ▼                                                               │
│  ┌──────────────────────┐                                                   │
│  │ Step 3: tradeLoad    │                                                   │
│  │ (Tasklet)             │                                                   │
│  │                       │                                                   │
│  │ CSV → SQL Server      │                                                   │
│  │ (Trade table)         │                                                   │
│  └──────────────────────┘                                                   │
│                                                                             │
│  Components:                                                                │
│  - TradeInitTasklet                                                         │
│  - TradeCsvTasklet (exports PostgreSQL to CSV)                              │
│  - TradeLoadTasklet (loads CSV to SQL Server)                               │
│  - TradeCsvReaderConfig (alternative reader for chunk approach)             │
│  - TradeCsvWriterConfig (FlatFileItemWriter)                                 │
└─────────────────────────────────────────────────────────────────────────────┘

## Package Structure

```
jp.lrm.batch/
├── CsvBatch2Application.java          # Main Spring Boot application
├── config/
│   ├── PostgresDataSourceConfig.java  # PostgreSQL datasource (primary)
│   └── SqlServerDataSourceConfig.java # SQL Server datasource (LRM)
├── job/
│   ├── CustomerCsvJobConfig.java      # Customer data transfer job
│   ├── SecurityCsvJobConfig.java      # Security data transfer job
│   └── TradeCsvJobConfig.java         # Trade data transfer + CSV export job
├── model/
│   ├── Customer.java                  # Customer entity
│   ├── Security.java                  # Security entity
│   └── Trade.java                     # Trade entity
├── processor/
│   ├── CustomerProcessor.java         # Customer data transformation
│   ├── SecurityProcessor.java         # Security data transformation
│   └── TradeProcessor.java            # Trade data transformation
├── reader/
│   ├── CustomerCsvReaderConfig.java   # Reads from PostgreSQL
│   ├── SecurityCsvReaderConfig.java   # Reads from PostgreSQL
│   └── TradeCsvReaderConfig.java      # Reads from PostgreSQL
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
    ├── SecurityDbWriterConfig.java    # Write to SQL Server
    └── TradeCsvWriterConfig.java      # Write to CSV file
```

## Data Flow

### Customer & Security Jobs (Direct DB-to-DB Transfer)
```
PostgreSQL (upstream_customer/upstream_security)
    ↓ [JdbcCursorItemReader]
Customer/Security Processor (trim)
    ↓ [JdbcBatchItemWriter]
SQL Server (Customer/Security tables)
```

### Trade Job (PostgreSQL → CSV → SQL Server)
```
PostgreSQL (upstream_trade)
    ↓ [TradeCsvTasklet - SELECT query]
CSV File (target/trade.csv)
    ↓ [TradeLoadTasklet - parse CSV]
SQL Server (Trade table)
```

## Configuration

### Application Properties
- **PostgreSQL**: localhost:5432/postgres (primary datasource)
- **SQL Server**: localhost:1433/mini_jpa (LRM datasource)
- **Batch Metadata**: Auto-initialize schema
- **Logging**: Spring Batch INFO level

### SQL Scripts
- `sql/lrm/customer_truncate.sql` - TRUNCATE Customer table
- `sql/lrm/security_truncate.sql` - TRUNCATE Security table
- `sql/lrm/trade_truncate.sql` - TRUNCATE Trade table
- `sql/upstream/trade_select.sql` - SELECT query for trade data

## Key Design Patterns

1. **Dual DataSource Configuration**: Separate datasources for PostgreSQL (primary) and SQL Server with dedicated transaction managers
2. **Chunk-oriented Processing**: Customer and Security jobs use chunk processing (100 items/chunk) for performance
3. **Tasklet-based Processing**: Trade job uses tasklets for CSV export/import operations
4. **Bean Mapping**: Writers use beanMapped() for automatic parameter binding
5. **External SQL**: Trade reader loads SQL from classpath resource for maintainability
