package digital.metro.pricing.calculator.validator;

import digital.metro.pricing.calculator.dto.Basket;
import digital.metro.pricing.calculator.exception.CalculatorExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Validator {
    @Autowired
    private CalculatorExceptionHandler exceptionHandler;
    public void validate(Basket basket) {
        if(basket.getEntries() == null)
            throw new IllegalArgumentException("Basket should not be empty.");

        basket.getEntries().stream().forEach(be -> {
            BigDecimal quantity = be.getQuantity();
            if(quantity == null || BigDecimal.ZERO.compareTo(quantity) > 0) {
                throw new IllegalArgumentException("Quantity should be greater than zero");
            }

            String articleId = be.getArticleId();
            if(articleId == null) {
                throw new IllegalArgumentException("Invalid article");
            }
        });
    }
}
