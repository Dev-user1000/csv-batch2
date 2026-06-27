package jp.lrm.batch.step;

import jp.lrm.batch.tasklet.SecurityInitTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SecurityInitStepConfig {

    @Bean
    public Step securityInitStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SecurityInitTasklet tasklet
    ) {
        return new StepBuilder("securityInitStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
