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
public class TradeLoadTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    @Value("${batch.trade.csv.path}")
    private String inputPath;

    public TradeLoadTasklet(
            @Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String dbName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
        System.out.println("接続DB = " + dbName);

        System.out.println("Trade CSV ロード開始…");

        List<String> lines = Files.readAllLines(Paths.get(inputPath));

        // ヘッダ削除
        lines.remove(0);

        String sql = """
            INSERT INTO Trade (
                baseDate, tradeNo, customerCode, securityCode,
                buySell, quantity, amount, contractDate, settlementDate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        for (String line : lines) {
            String[] cols = line.split(",");

            jdbcTemplate.update(sql,
                    cols[0],
                    cols[1],
                    cols[2],
                    cols[3],
                    cols[4],
                    Integer.parseInt(cols[5]),
                    Integer.parseInt(cols[6]),
                    cols[7],
                    cols[8]
            );
        }

        System.out.println("Trade CSV ロード完了。");

        return RepeatStatus.FINISHED;
    }
}
