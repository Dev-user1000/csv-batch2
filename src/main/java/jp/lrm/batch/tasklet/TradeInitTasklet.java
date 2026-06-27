package jp.lrm.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class TradeInitTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    public TradeInitTasklet(
            @Qualifier("sqlServerDataSource") DataSource dataSource,
            ResourceLoader resourceLoader) {

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.resourceLoader = resourceLoader;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Resource resource = resourceLoader.getResource("classpath:sql/lrm/trade_truncate.sql");

        String sql;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        System.out.println("Trade テーブル初期化 SQL 実行開始…");
        jdbcTemplate.execute(sql);
        System.out.println("Trade テーブル初期化 SQL 実行完了。");

        return RepeatStatus.FINISHED;
    }
}
