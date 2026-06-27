package jp.lrm.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerCsvJobConfig {

    @Bean
    public Job customerCsvJob(
            JobRepository jobRepository,
            @Qualifier("customerInitStep") Step customerInitStep,
            @Qualifier("customerCsvStep") Step customerCsvStep) {

        return new JobBuilder("customerCsvJob", jobRepository)
                .start(customerInitStep)   // TRUNCATE
                .next(customerCsvStep)     // PostgreSQL → SQL Server
                .build();
    }
}