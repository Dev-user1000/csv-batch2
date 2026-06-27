package jp.lrm.batch.step;

import jp.lrm.batch.tasklet.CustomerInitTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CustomerInitStepConfig {

    @Bean
    public Step customerInitStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            CustomerInitTasklet tasklet) {

        return new StepBuilder("customerInitStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}