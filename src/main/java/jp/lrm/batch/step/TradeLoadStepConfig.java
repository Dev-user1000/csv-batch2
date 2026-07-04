package jp.lrm.batch.step;

import jp.lrm.batch.tasklet.TradeLoadTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TradeLoadStepConfig {

    @Bean
    public Step tradeLoadStep(
            JobRepository jobRepository,
            @Qualifier("sqlServerTransactionManager") PlatformTransactionManager transactionManager,
            TradeLoadTasklet tasklet
    ) {
        return new StepBuilder("tradeLoadStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .allowStartIfComplete(true)   // ★ 前回 COMPLETED でも必ず実行
                .build();
    }
}
