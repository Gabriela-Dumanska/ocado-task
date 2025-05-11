package pl.edu.agh;

import pl.edu.agh.factory.OrderFactory;
import pl.edu.agh.factory.PaymentMethodFactory;
import pl.edu.agh.algorithm.GreedyPaymentOptimizer;
import pl.edu.agh.model.Order;
import pl.edu.agh.model.PaymentMethod;
import pl.edu.agh.exception.ErrorException;
import pl.edu.agh.exception.WarningException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.severe("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }
        Path ordersPath = Paths.get(args[0]);
        Path methodsPath = Paths.get(args[1]);
        List<Order> orders;
        List<PaymentMethod> methods;
        try {
            orders = OrderFactory.fromJson(ordersPath);
            methods = PaymentMethodFactory.fromJson(methodsPath);
        } catch (WarningException we) {
            logger.warning("Configuration warning: " + we.getMessage());
            System.exit(2);
            return;
        } catch (IOException ioe) {
            logger.severe("I/O error: " + ioe.getMessage());
            System.exit(3);
            return;
        }
        GreedyPaymentOptimizer optimizer = new GreedyPaymentOptimizer();
        List<PaymentMethod> result;
        try {
            result = optimizer.optimizePayments(orders, methods);
        } catch (ErrorException ee) {
            logger.severe("Critical processing error: " + ee.getMessage());
            System.exit(4);
            return;
        }
        for (PaymentMethod pm : result) {
            BigDecimal used = pm.getLimit().subtract(pm.getRemaining());
            if (used.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println(pm.getId() + " " + used.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }
}
