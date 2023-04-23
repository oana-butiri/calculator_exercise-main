package digital.metro.pricing.calculator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@AllArgsConstructor
public class BasketCalculationResult {
    private String customerId;
    private Map<String, BigDecimal> pricedBasketEntries;
    private BigDecimal totalAmount;
}
