package jp.lrm.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class TradeCsvJobConfig {

    @Bean
    public Job tradeCsvJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("tradeInitStep") Step tradeInitStep,
            @Qualifier("tradeCsvStep") Step tradeCsvStep,
            @Qualifier("tradeLoadStep") Step tradeLoadStep
    ) {
        return new JobBuilder("tradeCsvJob", jobRepository)
                .start(tradeInitStep)
                .next(tradeCsvStep)
                .next(tradeLoadStep)
                .build();
    }
}
