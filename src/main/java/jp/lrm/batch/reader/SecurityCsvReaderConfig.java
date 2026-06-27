package jp.lrm.batch.reader;

import jp.lrm.batch.model.Security;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SecurityCsvReaderConfig {

    @Bean
    public JdbcCursorItemReader<Security> securityCsvReader(DataSource postgresDataSource) {

        String sql = """
            SELECT
                base_date      AS baseDate,
                security_code  AS securityCode,
                security_name  AS securityName
            FROM upstream_security
        """;

        return new JdbcCursorItemReaderBuilder<Security>()
                .name("securityCsvReader")
                .dataSource(postgresDataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> {
                    Security s = new Security();
                    s.setBaseDate(rs.getString("baseDate"));
                    s.setSecurityCode(rs.getString("securityCode"));
                    s.setSecurityName(rs.getString("securityName"));
                    return s;
                })
                .build();
    }
}
