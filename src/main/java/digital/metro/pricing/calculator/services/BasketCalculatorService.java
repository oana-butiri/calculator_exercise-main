package digital.metro.pricing.calculator.services;

import digital.metro.pricing.calculator.dto.Basket;
import digital.metro.pricing.calculator.dto.BasketCalculationResult;

import java.math.BigDecimal;

public interface BasketCalculatorService {
    BasketCalculationResult calculateBasket(Basket basket);
    BigDecimal getArticlePriceForCustomer(String articleId, String customerId);
}
