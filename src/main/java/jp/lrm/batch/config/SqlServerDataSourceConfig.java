package jp.lrm.batch.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SqlServerDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.mssql")
    public DataSourceProperties sqlServerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "sqlServerDataSource")
    public DataSource sqlServerDataSource() {
        return sqlServerDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    // ★ これが最重要
    @Bean(name = "sqlServerTransactionManager")
    public DataSourceTransactionManager sqlServerTransactionManager(
            @Qualifier("sqlServerDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
