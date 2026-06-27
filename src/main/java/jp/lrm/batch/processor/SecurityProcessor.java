package jp.lrm.batch.processor;

import jp.lrm.batch.model.Security;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class SecurityProcessor implements ItemProcessor<Security, Security> {

    @Override
    public Security process(Security item) {
        if (item.getSecurityName() != null) {
            item.setSecurityName(item.getSecurityName().trim());
        }
        return item;
    }
}
