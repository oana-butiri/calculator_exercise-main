package digital.metro.pricing.calculator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import digital.metro.pricing.calculator.dto.Basket;
import digital.metro.pricing.calculator.dto.BasketEntry;
import digital.metro.pricing.calculator.exception.ExceptionResponse;
import digital.metro.pricing.calculator.services.BasketCalculatorService;
import digital.metro.pricing.calculator.validator.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalculatorResourceControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BasketCalculatorService basketCalculatorService;
    @Autowired
    private Validator validator;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenQuantityIsValid_thenReturnsStatus200() throws Exception {
        Basket basket = getBasket();

        MvcResult mvcResult = mvc.perform(post("/baskets")
                                        .content(asJsonString(basket))
                                        .contentType(APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void whenQuantityIsInvalid_thenReturnsStatus400() throws Exception {
        Basket basket = getInvalidBasket();

        MvcResult mvcResult = mvc.perform(post("/baskets")
                        .content(asJsonString(basket))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(new ExceptionResponse("Quantity should be greater than zero")));
    }

    @Test
    void whenGetArticlePrice_thenReturnsStatus200() throws Exception {
        MvcResult mvcResult = mvc.perform(get("/articles/article-5/price"))
                .andExpect(status().isOk()).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void whenGetArticlePriceForCustomer_thenReturnsStatus200() throws Exception {
        MvcResult mvcResult = mvc.perform(get("/articles/article-5/price?customerId=customer-1"))
                .andExpect(status().isOk()).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Basket getBasket() {
        return new Basket("customer-1", Set.of(
                new BasketEntry("article-1", new BigDecimal("2"))));
    }

    private Basket getInvalidBasket() {
        return new Basket("customer-1", Set.of(
                new BasketEntry("article-1", new BigDecimal("-1"))));
    }
}
