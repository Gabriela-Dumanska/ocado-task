package pl.edu.agh.algorithm;

import pl.edu.agh.exception.ErrorException;
import pl.edu.agh.model.Order;
import pl.edu.agh.model.PaymentMethod;
import java.math.BigDecimal;

public class AssignmentOption {
    public enum Type { CARD, PARTIAL_POINTS, FULL_POINTS }

    private final Order order;
    private final PaymentMethod paymentMethod;
    private final Type type;
    private final BigDecimal profit;
    private final BigDecimal cost;

    public AssignmentOption(Order order, PaymentMethod paymentMethod, Type type, BigDecimal profit, BigDecimal cost) {
        if (order == null || paymentMethod == null || type == null || profit == null || cost == null) {
            throw new ErrorException("AssignmentOption parameters must not be null");
        }
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.type = type;
        this.profit = profit;
        this.cost = cost;
    }

    public Order getOrder() { return order; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public Type getType() { return type; }
    public BigDecimal getProfit() { return profit; }
    public BigDecimal getCost() { return cost; }

    public BigDecimal getProfitDensity() {
        if (cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
        return profit.divide(cost, 10, BigDecimal.ROUND_HALF_UP);
    }
}