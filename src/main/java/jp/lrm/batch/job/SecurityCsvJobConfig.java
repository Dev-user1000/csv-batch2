package jp.lrm.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SecurityCsvJobConfig {

    @Bean
    public Job securityCsvJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Step securityInitStep,
            Step securityCsvStep
    ) {
        return new JobBuilder("securityCsvJob", jobRepository)
                .start(securityInitStep)
                .next(securityCsvStep)
                .build();
    }
}
