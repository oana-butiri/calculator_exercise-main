package digital.metro.pricing.calculator.services;

import digital.metro.pricing.calculator.dto.Basket;
import digital.metro.pricing.calculator.dto.BasketCalculationResult;
import digital.metro.pricing.calculator.dto.BasketEntry;
import digital.metro.pricing.calculator.exception.ArticleNotFoundException;
import digital.metro.pricing.calculator.repositories.DiscountRepository;
import digital.metro.pricing.calculator.repositories.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BasketCalculatorServiceTest {

    @Mock
    private PriceRepository mockPriceRepository;
    @Mock
    private DiscountRepository mockDiscountRepository;

    private BasketCalculatorService service;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        service = new BasketCalculatorServiceImpl(mockPriceRepository, mockDiscountRepository);
    }

    @Test
    public void testGetArticlePriceWhenCustomerIdIsNull() {
        // GIVEN
        String articleId = "article-1";
        BigDecimal price = new BigDecimal("34.29");
        when(mockPriceRepository.findPriceByArticleId(articleId)).thenReturn(price);

        // WHEN
        var result = service.getArticlePriceForCustomer(articleId, null);

        // THEN
        assertThat(result).isEqualByComparingTo(price);
    }

    @Test
    public void testGetArticlePriceWhenCustomerHasDiscount() {
        // GIVEN
        var articleId = "article-1";
        var standardPrice = new BigDecimal("34.29");
        var customerPrice = new BigDecimal("30.86");
        var customerId = "customer-1";

        when(mockPriceRepository.findPriceByArticleId(articleId)).thenReturn(standardPrice);
        when(mockDiscountRepository.findDiscountByCustomerId(customerId)).thenReturn(Optional.of(new BigDecimal("0.9")));

        // WHEN
        var result = service.getArticlePriceForCustomer(articleId, "customer-1");

        // THEN
        assertThat(result).isEqualByComparingTo(customerPrice);
    }

    @Test
    public void testCalculateBasketWhenCustomerHasDiscount() {
        // GIVEN
        var basket = getBasket(new BigDecimal("4"), new BigDecimal("2"), BigDecimal.ONE);

        var prices = getPrices(new BigDecimal("1.50"), new BigDecimal("0.58"), new BigDecimal("9.99"));

        var pricedArticles = getPricedArticles(new BigDecimal("5.40"), new BigDecimal("1.04"), new BigDecimal("8.99"));

        when(mockPriceRepository.findPriceByArticleId("article-1")).thenReturn(prices.get("article-1"));
        when(mockPriceRepository.findPriceByArticleId("article-2")).thenReturn(prices.get("article-2"));
        when(mockPriceRepository.findPriceByArticleId("article-3")).thenReturn(prices.get("article-3"));
        when(mockDiscountRepository.findDiscountByCustomerId(any())).thenReturn(Optional.of(new BigDecimal("0.9")));

        // WHEN
        var result = service.calculateBasket(basket);

        // THEN
        assertThat(result.getCustomerId()).isEqualTo("customer-1");
        assertThat(result.getPricedBasketEntries()).isEqualTo(pricedArticles);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("15.43"));
    }

    @Test
    public void testCalculateBasketWhenCustomerHasNoDiscount() {
        // GIVEN
        var basket = getBasketForNonDiscountedCustomer(new BigDecimal("4"), new BigDecimal("2"), BigDecimal.ONE);

        var prices = getPrices(new BigDecimal("1.50"), new BigDecimal("0.58"), new BigDecimal("9.99"));

        var pricedArticles = getPricedArticles(new BigDecimal("6.00"), new BigDecimal("1.16"), new BigDecimal("9.99"));

        when(mockPriceRepository.findPriceByArticleId("article-1")).thenReturn(prices.get("article-1"));
        when(mockPriceRepository.findPriceByArticleId("article-2")).thenReturn(prices.get("article-2"));
        when(mockPriceRepository.findPriceByArticleId("article-3")).thenReturn(prices.get("article-3"));
        when(mockDiscountRepository.findDiscountByCustomerId(any())).thenReturn(Optional.of(BigDecimal.ONE));

        // WHEN
        var result = service.calculateBasket(basket);

        // THEN
        assertThat(result.getCustomerId()).isEqualTo("customer-3");
        assertThat(result.getPricedBasketEntries()).isEqualTo(pricedArticles);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("17.15"));
    }

    @Test
    public void testCalculateBasketWhenArticlePriceIsNotFound() {
        // GIVEN
        var basket = getBasket(new BigDecimal(1), BigDecimal.ONE, BigDecimal.ONE);

        // WHEN
        var exception = assertThrows(ArticleNotFoundException.class, () -> {
            service.calculateBasket(basket);
        });

        var expectedMessage = "Could not find price for article";
        var actualMessage = exception.getMessage();
        assertEquals(ArticleNotFoundException.class, exception.getClass());
        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void testCalculateBasketWhenQuantityIsMoreThanMaxQuantity() {
        // GIVEN
        var basket = getBasket(new BigDecimal(11), BigDecimal.ONE, BigDecimal.ONE);
        var prices = getPrices(new BigDecimal("1.50"), new BigDecimal("0.58"), new BigDecimal("9.99"));
        when(mockPriceRepository.findPriceByArticleId("article-1")).thenReturn(prices.get("article-1"));
        when(mockPriceRepository.findPriceByArticleId("article-2")).thenReturn(prices.get("article-2"));
        when(mockPriceRepository.findPriceByArticleId("article-3")).thenReturn(prices.get("article-3"));
        // WHEN
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            service.calculateBasket(basket);
        });

        var expectedMessage = "Quantity 11 exceeds the available amount";
        var actualMessage = exception.getMessage();
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertEquals(expectedMessage, actualMessage);

    }

    private Map<String, BigDecimal> getPricedArticles(BigDecimal price1, BigDecimal price2, BigDecimal price3) {
        var pricedArticles = Map.of(
                "article-1", price1,
                "article-2", price2,
                "article-3", price3);
        return pricedArticles;
    }

    private Map<String, BigDecimal> getPrices(BigDecimal price1, BigDecimal price2, BigDecimal price3) {
        var prices = Map.of(
                "article-1", price1,
                "article-2", price2,
                "article-3", price3);
        return prices;
    }

    private Basket getBasket(BigDecimal article1Quantity, BigDecimal article2Quantity, BigDecimal article3Quantity) {
        var basket = new Basket("customer-1", Set.of(
                new BasketEntry("article-1", article1Quantity),
                new BasketEntry("article-2", article2Quantity),
                new BasketEntry("article-3", article3Quantity)));
        return basket;
    }

    private Basket getBasketForNonDiscountedCustomer(BigDecimal article1Quantity, BigDecimal article2Quantity, BigDecimal article3Quantity) {
        var basket = new Basket("customer-3", Set.of(
                new BasketEntry("article-1", article1Quantity),
                new BasketEntry("article-2", article2Quantity),
                new BasketEntry("article-3", article3Quantity)));
        return basket;
    }
}
