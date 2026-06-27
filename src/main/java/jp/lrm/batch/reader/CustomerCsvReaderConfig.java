package jp.lrm.batch.reader;

import jp.lrm.batch.model.Customer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CustomerCsvReaderConfig {

    @Bean
    public JdbcCursorItemReader<Customer> customerCsvReader(DataSource postgresDataSource) {

        String sql = """
            SELECT
                base_date      AS baseDate,
                customer_code  AS customerCode,
                customer_name  AS customerName
            FROM upstream_customer
        """;

        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerCsvReader")
                .dataSource(postgresDataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> {
                    Customer c = new Customer();
                    c.setBaseDate(rs.getString("baseDate"));
                    c.setCustomerCode(rs.getString("customerCode"));
                    c.setCustomerName(rs.getString("customerName"));
                    return c;
                })
                .build();
    }
}