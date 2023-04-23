package digital.metro.pricing.calculator.repositories;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class DiscountRepository {
    //TODO: this should be cached
    private Map<String, BigDecimal> customerDiscountsPercentages = Map.of("customer-1", new BigDecimal("0.90"),
            "customer-2",  new BigDecimal("0.85"));

    public Optional<BigDecimal> findDiscountByCustomerId(String customerId) {
        return customerDiscountsPercentages.entrySet().stream()
                .filter(e -> StringUtils.equals(e.getKey(), customerId))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
