package dev.example.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.example.domain.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {
    private final PaymentService service = new PaymentService();

    @Test
    void authorizesPositiveAmounts() {
        assertEquals(
                "AUTHORIZED",
                service.authorize(new Money(new BigDecimal("19.99"))));
    }
}
