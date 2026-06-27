package jp.lrm.batch.processor;

import jp.lrm.batch.model.Trade;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TradeProcessor implements ItemProcessor<Trade, Trade> {

    @Override
    public Trade process(Trade item) {
        if (item.getBuySell() != null) {
            item.setBuySell(item.getBuySell().trim());
        }
        return item;
    }
}
