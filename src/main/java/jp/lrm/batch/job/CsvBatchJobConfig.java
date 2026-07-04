package jp.lrm.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CsvBatchJobConfig {

    @Bean
    public Job csvBatchJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step customerInitStep,
            Step customerCsvStep,
            Step securityInitStep,
            Step securityCsvStep,
            Step tradeInitStep,
            Step tradeCsvStep,
            Step tradeLoadStep
    ) {
        return new JobBuilder("csvBatchJob", jobRepository)
                .start(customerInitStep)
                .next(customerCsvStep)
                .next(securityInitStep)
                .next(securityCsvStep)
                .next(tradeInitStep)
                .next(tradeCsvStep)
                .next(tradeLoadStep)
                .build();
    }
}