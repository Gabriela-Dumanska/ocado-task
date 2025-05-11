package pl.edu.agh;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Order> orders = Arrays.asList(
                new Order("ORDER1", new BigDecimal("100.00"), Arrays.asList("mZysk")),
                new Order("ORDER2", new BigDecimal("200.00"), Arrays.asList("BosBankrut")),
                new Order("ORDER3", new BigDecimal("150.00"), Arrays.asList("mZysk", "BosBankrut")),
                new Order("ORDER4", new BigDecimal("50.00"), null)
        );

        List<PaymentMethod> paymentMethods = Arrays.asList(
                new PaymentMethod("PUNKTY", new BigDecimal("15"), new BigDecimal("100.00")),
                new PaymentMethod("mZysk", new BigDecimal("10"), new BigDecimal("180.00")),
                new PaymentMethod("BosBankrut", new BigDecimal("5"), new BigDecimal("200.00"))
        );

        GreedyPaymentOptimizer optimizer = new GreedyPaymentOptimizer();
        List<PaymentMethod> resultMethods = optimizer.optimizePayments(orders, paymentMethods);

        for (PaymentMethod method : resultMethods) {
            BigDecimal used = method.getLimit().subtract(method.getRemaining());
            if (used.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println(method.getId() + " " + used.setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }
    }
}
