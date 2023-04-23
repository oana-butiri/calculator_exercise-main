package digital.metro.pricing.calculator.controllers;

import digital.metro.pricing.calculator.dto.Basket;
import digital.metro.pricing.calculator.dto.BasketCalculationResult;
import digital.metro.pricing.calculator.services.BasketCalculatorService;
import digital.metro.pricing.calculator.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class CalculatorResourceController {

    private BasketCalculatorService basketCalculatorService;
    private Validator validator;

    @Autowired
    public CalculatorResourceController(BasketCalculatorService basketCalculatorService,
                                        Validator validator) {
        this.basketCalculatorService = basketCalculatorService;
        this.validator = validator;
    }

    @PostMapping("/baskets")
    public ResponseEntity<BasketCalculationResult> calculateBasket(@RequestBody Basket basket) {
        validator.validate(basket);
        BasketCalculationResult response = basketCalculatorService.calculateBasket(basket);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/articles/{articleId}/price")
    public ResponseEntity<BigDecimal> getArticlePrice(@PathVariable String articleId, @RequestParam(required = false) String customerId) {
        BigDecimal response = basketCalculatorService.getArticlePriceForCustomer(articleId, customerId);
        return ResponseEntity.ok().body(response);
    }
}
