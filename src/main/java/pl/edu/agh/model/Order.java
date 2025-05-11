package pl.edu.agh.model;

import java.math.BigDecimal;
import java.util.List;
import pl.edu.agh.exception.WarningException;

public class Order {
    private final String id;
    private final BigDecimal value;
    private final List<String> promotions;

    public Order(String id, BigDecimal value, List<String> promotions) throws WarningException {
        if (id == null || id.isBlank()) {
            throw new WarningException("Order id must not be empty");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WarningException("Order value must be positive for id=" + id);
        }
        this.id = id;
        this.value = value;
        this.promotions = promotions;
    }

    public String getId() { return id; }
    public BigDecimal getValue() { return value; }
    public List<String> getPromotions() { return promotions; }
}