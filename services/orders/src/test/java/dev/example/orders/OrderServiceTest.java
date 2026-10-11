package dev.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.example.domain.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderServiceTest {
    private final OrderService service = new OrderService();

    @Test
    void calculatesAnOrderTotal() {
        Money total = service.total(new Money(new BigDecimal("7.25")), 4);

        assertEquals(new BigDecimal("29.00"), total.amount());
    }
}
