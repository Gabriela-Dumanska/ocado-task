package pl.edu.agh;

import java.math.BigDecimal;

public class PaymentMethod {
    private final String id;
    private final BigDecimal discount;
    private final BigDecimal limit;
    private BigDecimal remaining;

    public PaymentMethod(String id, BigDecimal discount, BigDecimal limit) {
        this.id = id;
        this.discount = discount;
        this.limit = limit;
        this.remaining = limit;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void useAmount(BigDecimal amount) {
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Amount exceeds remaining limit for method: " + id);
        }
        this.remaining = this.remaining.subtract(amount);
    }
}
