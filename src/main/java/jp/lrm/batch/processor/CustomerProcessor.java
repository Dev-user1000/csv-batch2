package jp.lrm.batch.processor;

import jp.lrm.batch.model.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomerProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item) {
        // 必要に応じて変換処理を書く
        if (item.getCustomerName() != null) {
            item.setCustomerName(item.getCustomerName().trim());
        }
        return item;
    }
}