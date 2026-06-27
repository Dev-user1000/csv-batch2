package jp.lrm.batch.writer;

import jp.lrm.batch.model.Customer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

@Configuration
public class CustomerDbWriterConfig {

    @Bean
    public JdbcBatchItemWriter<Customer> customerDbWriter(
            @Qualifier("sqlServerDataSource") DataSource sqlServerDataSource) {

        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(sqlServerDataSource)  // ★ SQL Server を強制指定
                .sql("""
                    INSERT INTO Customer (baseDate, customerCode, customerName)
                    VALUES (:baseDate, :customerCode, :customerName)
                """)
                .beanMapped()
                .build();
    }
}