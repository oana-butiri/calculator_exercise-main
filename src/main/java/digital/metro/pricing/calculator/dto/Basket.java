package digital.metro.pricing.calculator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class Basket {
    private String customerId;
    private Set<BasketEntry> entries;
}
