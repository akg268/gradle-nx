package dev.example.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test
    void multipliesAnAmount() {
        Money total = new Money(new BigDecimal("12.50")).multiply(3);

        assertEquals(new BigDecimal("37.50"), total.amount());
    }
}
