package jp.lrm.batch.step;

import jp.lrm.batch.tasklet.TradeCsvTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TradeCsvStepConfig {

    @Bean
    public Step tradeCsvStep(
            JobRepository jobRepository,
            @Qualifier("postgresTransactionManager") PlatformTransactionManager transactionManager,
            TradeCsvTasklet tasklet
    ) {
        return new StepBuilder("tradeCsvStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .allowStartIfComplete(true)   // ★ 前回 COMPLETED でも必ず実行
                .build();
    }
}
