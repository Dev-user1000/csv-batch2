package jp.lrm.batch.step;

import jp.lrm.batch.model.Security;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;

@Configuration
public class SecurityCsvStepConfig {

    @Bean
    public Step securityCsvStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Security> securityCsvReader,
            ItemProcessor<Security, Security> securityProcessor,
            ItemWriter<Security> securityDbWriter
    ) {
        return new StepBuilder("securityCsvStep", jobRepository)
                .<Security, Security>chunk(10, transactionManager)
                .reader(securityCsvReader)
                .processor(securityProcessor)
                .writer(securityDbWriter)
                .build();
    }
}
