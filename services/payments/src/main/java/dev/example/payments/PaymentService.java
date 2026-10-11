package dev.example.payments;

import dev.example.domain.Money;

public final class PaymentService {
    public String authorize(Money amount) {
        if (amount.amount().signum() <= 0) {
            return "REJECTED";
        }
        return "AUTHORIZED";
    }
}
