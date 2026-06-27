package jp.lrm.batch.step;

import jp.lrm.batch.model.Customer;
import jp.lrm.batch.processor.CustomerProcessor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class CustomerCsvStepConfig {

    @Bean
    public Step customerCsvStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            @Qualifier("customerCsvReader") ItemReader<Customer> reader,
            CustomerProcessor processor,
            @Qualifier("customerDbWriter") ItemWriter<Customer> writer) {

        return new StepBuilder("customerCsvStep", jobRepository)
                .<Customer, Customer>chunk(100, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}