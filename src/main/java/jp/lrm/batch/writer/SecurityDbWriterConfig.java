package jp.lrm.batch.writer;

import jp.lrm.batch.model.Security;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

@Configuration
public class SecurityDbWriterConfig {

    @Bean
    public JdbcBatchItemWriter<Security> securityDbWriter(
            @Qualifier("sqlServerDataSource") DataSource sqlServerDataSource) {

        return new JdbcBatchItemWriterBuilder<Security>()
                .dataSource(sqlServerDataSource)
                .sql("""
                    INSERT INTO Security (baseDate, securityCode, securityName)
                    VALUES (:baseDate, :securityCode, :securityName)
                """)
                .beanMapped()
                .build();
    }
}
