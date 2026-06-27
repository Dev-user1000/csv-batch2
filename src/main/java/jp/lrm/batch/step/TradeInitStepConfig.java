package jp.lrm.batch.step;

import jp.lrm.batch.tasklet.TradeInitTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TradeInitStepConfig {

    @Bean
    public Step tradeInitStep(
            JobRepository jobRepository,
            @Qualifier("sqlServerTransactionManager") PlatformTransactionManager transactionManager,
            TradeInitTasklet tasklet
    ) {
        return new StepBuilder("tradeInitStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
