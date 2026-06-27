package jp.lrm.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class TradeLoadTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public TradeLoadTasklet(
            @Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        // ★ どの DB に接続しているか確認
        String dbName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
        System.out.println("接続DB = " + dbName);

        System.out.println("Trade CSV ロード開始…");

        // ★ target の CSV を読む
        List<String> lines = Files.readAllLines(
                Paths.get("C:/AIDev/Maven/Projects/csv-batch2/target/trade.csv")
        );

        // 1行目はヘッダなので削除
        lines.remove(0);

        // ★ Trade テーブルの列と完全一致した INSERT 文
        String sql = """
            INSERT INTO Trade (
                baseDate, tradeNo, customerCode, securityCode,
                buySell, quantity, amount, contractDate, settlementDate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        for (String line : lines) {
            String[] cols = line.split(",");

            jdbcTemplate.update(sql,
                    cols[0],  // baseDate
                    cols[1],  // tradeNo
                    cols[2],  // customerCode
                    cols[3],  // securityCode
                    cols[4],  // buySell
                    Integer.parseInt(cols[5]), // quantity
                    Integer.parseInt(cols[6]), // amount
                    cols[7],  // contractDate
                    cols[8]   // settlementDate
            );
        }

        System.out.println("Trade CSV ロード完了。");

        return RepeatStatus.FINISHED;
    }
}
