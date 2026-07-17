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
