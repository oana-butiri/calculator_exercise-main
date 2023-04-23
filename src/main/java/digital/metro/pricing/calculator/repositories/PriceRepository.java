package digital.metro.pricing.calculator.repositories;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A dummy implementation for testing purposes. In production, we would get real prices from a database.
 */
@Component
public class PriceRepository {
    private Map<String, BigDecimal> prices = new HashMap<>();
    private Random random = new Random();

    public BigDecimal findPriceByArticleId(String articleId) {
        return prices.computeIfAbsent(articleId,
                key -> getPricingByArticleId());
    }

    private BigDecimal getPricingByArticleId() {
        var randomValue = random.nextDouble();
        return BigDecimal.valueOf(0.5d)
                .add(BigDecimal.valueOf(randomValue).multiply(BigDecimal.valueOf(29.50d)));
    }
}
