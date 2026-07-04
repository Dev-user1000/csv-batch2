package jp.lrm.batch.reader;

import jp.lrm.batch.model.Trade;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Configuration
public class TradeCsvReaderConfig {

    @Bean
    public JdbcCursorItemReader<Trade> tradeCsvReader(DataSource postgresDataSource) throws Exception {

        // classpath から SQL を読み込む
        ClassPathResource resource = new ClassPathResource("sql/upstream/trade_select.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return new JdbcCursorItemReaderBuilder<Trade>()
                .name("tradeCsvReader")
                .dataSource(postgresDataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> {
                    Trade t = new Trade();
                    t.setBaseDate(rs.getString("base_date"));
                    t.setTradeNo(rs.getString("trade_no"));
                    t.setCustomerCode(rs.getString("customer_code"));
                    t.setSecurityCode(rs.getString("security_code"));
                    t.setBuySell(rs.getString("buy_sell"));
                    t.setQuantity(rs.getInt("quantity"));
                    t.setAmount(rs.getInt("amount"));
                    t.setContractDate(rs.getString("contract_date"));
                    t.setSettlementDate(rs.getString("settlement_date"));
                    return t;
                })
                .build();
    }
}
