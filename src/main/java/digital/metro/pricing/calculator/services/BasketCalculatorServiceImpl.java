package digital.metro.pricing.calculator.services;

import digital.metro.pricing.calculator.aop.LogExecutionTime;
import digital.metro.pricing.calculator.dto.Basket;
import digital.metro.pricing.calculator.dto.BasketCalculationResult;
import digital.metro.pricing.calculator.dto.BasketEntry;
import digital.metro.pricing.calculator.exception.ArticleNotFoundException;
import digital.metro.pricing.calculator.repositories.DiscountRepository;
import digital.metro.pricing.calculator.repositories.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BasketCalculatorServiceImpl implements BasketCalculatorService {

    private PriceRepository priceRepository;
    private DiscountRepository discountRepository;

    @Autowired
    public BasketCalculatorServiceImpl(PriceRepository priceRepository,
                                       DiscountRepository discountRepository) {
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @LogExecutionTime
    public BasketCalculationResult calculateBasket(Basket basket) {
        var pricedArticles = getPricedArticles(basket);
        var totalAmount = getTotalAmount(pricedArticles);
        log.debug("Total amount is: {}", totalAmount);

        return new BasketCalculationResult(basket.getCustomerId(), pricedArticles, totalAmount);
    }

    public BigDecimal getArticlePriceForCustomer(String articleId, String customerId) {
        if (customerId == null)
            return getFullPrice(articleId);

        return getPriceWithDiscountForCustomer(articleId, customerId);
    }

    // region Privates

    /**
     * Get quantity from db for article and check if the existing quantity is exceeded.
     * For now, I will just throw an exception every time quantity is greater than 10 for any article.
     */

    private BigDecimal getPriceWithDiscountForCustomer(String articleId, String customerId) {
        BigDecimal fullPrice = getFullPrice(articleId);

        var discountByCustomerId = discountRepository.findDiscountByCustomerId(customerId);
        if (discountByCustomerId.isEmpty())
            return fullPrice;

        var priceWithDiscount = fullPrice.multiply(discountByCustomerId.get());
        return getRoundedResult(priceWithDiscount);
    }

    private BigDecimal getFullPrice(String articleId) {
        var fullPrice = priceRepository.findPriceByArticleId(articleId);
        if(fullPrice == null) {
            throw new ArticleNotFoundException(MessageFormat.format("Could not find price for article {0}", articleId));
        }
        return fullPrice;
    }

    private BigDecimal getRoundedResult(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getTotalAmount(Map<String, BigDecimal> pricedArticles) {
        return pricedArticles.values().stream()
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    private Map<String, BigDecimal> getPricedArticles(Basket basket) {
        return basket.getEntries().stream()
                .collect(Collectors.toMap(
                        BasketEntry::getArticleId,
                        entry -> calculateArticleTotalPrice(entry, basket.getCustomerId())));
    }

    private BigDecimal calculateArticleTotalPrice(BasketEntry basketEntry, String customerId) {
        var quantity = basketEntry.getQuantity();

        if (!isQuantityValid(quantity)) {
            throw new IllegalArgumentException(MessageFormat.format("Quantity {0} exceeds the available amount", quantity.toString()));
        }

        var pricePerItem = getArticlePriceForCustomer(basketEntry.getArticleId(), customerId);
        return quantity.multiply(pricePerItem);
    }

    private boolean isQuantityValid(BigDecimal quantityByArticleId) {
        return BigDecimal.TEN.compareTo(quantityByArticleId) > -1;
    }

    // endregion Privates
}
