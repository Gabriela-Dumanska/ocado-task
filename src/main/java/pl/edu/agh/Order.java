package pl.edu.agh;

import java.math.BigDecimal;
import java.util.List;

public class Order {
    private final String id;
    private final BigDecimal value;
    private final List<String> promotions;

    public Order(String id, BigDecimal value, List<String> promotions) {
        this.id = id;
        this.value = value;
        this.promotions = promotions;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public List<String> getPromotions() {
        return promotions;
    }
}
