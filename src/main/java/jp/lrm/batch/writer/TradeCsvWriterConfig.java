package jp.lrm.batch.writer;

import jp.lrm.batch.model.Trade;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class TradeCsvWriterConfig {

    @Bean
    public FlatFileItemWriter<Trade> tradeCsvWriter() {

        return new FlatFileItemWriterBuilder<Trade>()
                .name("tradeCsvWriter")
                .resource(new FileSystemResource("target/trade.csv"))
                .delimited()
                .delimiter(",")
                .names(
                        "baseDate",
                        "tradeNo",
                        "customerCode",
                        "securityCode",
                        "buySell",
                        "quantity",
                        "amount",
                        "contractDate",
                        "settlementDate"
                )
                .headerCallback(writer -> writer.write(
                        "baseDate,tradeNo,customerCode,securityCode,buySell,quantity,amount,contractDate,settlementDate"
                ))
                .lineSeparator("\n")  // ★ 最終行に改行を付ける
                .build();
    }
}
