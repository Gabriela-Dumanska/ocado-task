package pl.edu.agh;

import pl.edu.agh.factory.OrderFactory;
import pl.edu.agh.factory.PaymentMethodFactory;
import pl.edu.agh.algorithm.GreedyPaymentOptimizer;
import pl.edu.agh.model.Order;
import pl.edu.agh.model.PaymentMethod;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }
        Path ordersPath = Paths.get(args[0]);
        Path methodsPath = Paths.get(args[1]);
        List<Order> orders;
        List<PaymentMethod> methods;
        try {
            orders = OrderFactory.fromJson(ordersPath);
            methods = PaymentMethodFactory.fromJson(methodsPath);
        } catch (IOException e) {
            System.err.println("Error reading JSON files: " + e.getMessage());
            return;
        }
        GreedyPaymentOptimizer optimizer = new GreedyPaymentOptimizer();
        List<PaymentMethod> result = optimizer.optimizePayments(orders, methods);
        for (PaymentMethod pm : result) {
            BigDecimal used = pm.getLimit().subtract(pm.getRemaining());
            if (used.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println(pm.getId() + " " + used.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }
}