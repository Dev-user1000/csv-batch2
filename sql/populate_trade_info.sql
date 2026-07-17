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
