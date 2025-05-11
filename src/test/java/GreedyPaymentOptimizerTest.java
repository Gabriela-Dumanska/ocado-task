import org.junit.jupiter.api.Test;
import pl.edu.agh.algorithm.GreedyPaymentOptimizer;
import pl.edu.agh.exception.ErrorException;
import pl.edu.agh.exception.WarningException;
import pl.edu.agh.model.Order;
import pl.edu.agh.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreedyPaymentOptimizerTest {

    private final GreedyPaymentOptimizer optimizer = new GreedyPaymentOptimizer();

    @Test
    void testOptimizePayments_nullListsThrowsError() {
        assertThrows(ErrorException.class, () -> optimizer.optimizePayments(null, new ArrayList<>()));
        assertThrows(ErrorException.class, () -> optimizer.optimizePayments(new ArrayList<>(), null));
    }

    @Test
    void testOptimizePayments_noPointsMethod_onlyCardUsed() throws WarningException {
        Order order = new Order("o1", new BigDecimal("100"), List.of("CARD1"));
        PaymentMethod card = new PaymentMethod("CARD1", new BigDecimal("10"), new BigDecimal("100"));
        List<Order> orders = List.of(order);
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(card);

        optimizer.optimizePayments(orders, methods);

        assertEquals(new BigDecimal("10.00"), card.getRemaining());
    }

    @Test
    void testOptimizePayments_withPointsMethod_partialPointsPreference() throws WarningException {
        Order order = new Order("o1", new BigDecimal("100"), null);
        PaymentMethod punkty = new PaymentMethod("PUNKTY", new BigDecimal("50"), new BigDecimal("100"));
        PaymentMethod card = new PaymentMethod("CARD1", new BigDecimal("10"), new BigDecimal("100"));
        List<Order> orders = List.of(order);
        List<PaymentMethod> methods = new ArrayList<>(List.of(punkty, card));

        optimizer.optimizePayments(orders, methods);

        assertEquals(new BigDecimal("10.00"), punkty.getRemaining());
        assertEquals(new BigDecimal("100"), card.getRemaining());
    }

    @Test
    void testOptimizePayments_fullPointsWhenBetterThanPartial() throws WarningException {
        Order order = new Order("o1", new BigDecimal("100"), null);
        PaymentMethod punkty = new PaymentMethod("PUNKTY", new BigDecimal("80"), new BigDecimal("100"));
        List<Order> orders = List.of(order);
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(punkty);

        optimizer.optimizePayments(orders, methods);

        assertEquals(new BigDecimal("80.00"), punkty.getRemaining());
    }

    @Test
    void testOptimizePayments_insufficientLimitAcrossOrders() throws WarningException {
        Order o1 = new Order("o1", new BigDecimal("100"), List.of("CARD"));
        Order o2 = new Order("o2", new BigDecimal("100"), List.of("CARD"));
        PaymentMethod card = new PaymentMethod("CARD", new BigDecimal("10"), new BigDecimal("100"));
        List<Order> orders = List.of(o1, o2);
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(card);

        optimizer.optimizePayments(orders, methods);

        assertEquals(new BigDecimal("0.00"), card.getRemaining());
    }
}
