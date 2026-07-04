package jp.lrm.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class TradeCsvTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    @Value("${batch.trade.csv.path}")
    private String outputPath;

    public TradeCsvTasklet(
            @Qualifier("postgresDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        System.out.println("Trade CSV 出力処理を開始します");
        System.out.println("Trade CSV 出力開始…");

        String sql = """
            SELECT
                base_date,
                trade_no,
                customer_code,
                security_code,
                buy_sell,
                quantity,
                amount,
                contract_date,
                settlement_date
            FROM upstream_trade
            ORDER BY trade_no
        """;

        List<String> lines = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return String.join(",",
                    rs.getString("base_date"),
                    rs.getString("trade_no"),
                    rs.getString("customer_code"),
                    rs.getString("security_code"),
                    rs.getString("buy_sell"),
                    rs.getString("quantity"),
                    rs.getString("amount"),
                    rs.getString("contract_date"),
                    rs.getString("settlement_date")
            );
        });

        // ヘッダ行
        lines.add(0,
                "baseDate,tradeNo,customerCode,securityCode,buySell,quantity,amount,contractDate,settlementDate"
        );

        Files.write(Paths.get(outputPath), lines);

        System.out.println("Trade CSV 出力完了。");

        return RepeatStatus.FINISHED;
    }
}
