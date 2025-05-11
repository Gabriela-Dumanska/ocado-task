package pl.edu.agh.model;

import java.math.BigDecimal;

import pl.edu.agh.exception.ErrorException;
import pl.edu.agh.exception.WarningException;

public class PaymentMethod {
    private final String id;
    private final BigDecimal discount;
    private final BigDecimal limit;
    private BigDecimal remaining;

    public PaymentMethod(String id, BigDecimal discount, BigDecimal limit) throws WarningException {
        if (id == null || id.isBlank()) {
            throw new WarningException("PaymentMethod id must not be empty");
        }
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new WarningException("Discount must be non-negative for method=" + id);
        }
        if (limit == null || limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new WarningException("Limit must be non-negative for method=" + id);
        }
        this.id = id;
        this.discount = discount;
        this.limit = limit;
        this.remaining = limit;
    }

    public String getId() { return id; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getLimit() { return limit; }
    public BigDecimal getRemaining() { return remaining; }

    public void useAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount to use must be non-negative");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new ErrorException("Insufficient limit in method=" + id + ": requested=" + amount + ", available=" + remaining);
        }
        remaining = remaining.subtract(amount);
    }
}