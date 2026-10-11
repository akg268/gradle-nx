package dev.example.orders;

import dev.example.domain.Money;

public final class OrderService {
    public Money total(Money unitPrice, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        return unitPrice.multiply(quantity);
    }
}
