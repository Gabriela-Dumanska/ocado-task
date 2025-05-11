package pl.edu.agh.algorithm;

import pl.edu.agh.model.Order;
import pl.edu.agh.model.PaymentMethod;
import pl.edu.agh.exception.ErrorException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

public class GreedyPaymentOptimizer {
    private static final Logger logger = Logger.getLogger(GreedyPaymentOptimizer.class.getName());

    public List<PaymentMethod> optimizePayments(List<Order> orders, List<PaymentMethod> methods) {
        if (orders == null || methods == null) {
            throw new ErrorException("Orders or methods list must not be null");
        }
        PaymentMethod pointsMethod = methods.stream()
                .filter(m -> "PUNKTY".equals(m.getId()))
                .findFirst().orElse(null);
        if (pointsMethod == null) {
            logger.warning("No points method found; partial/full points options disabled");
        }

        List<AssignmentOption> options = new ArrayList<>();
        for (Order order : orders) {
            BigDecimal value = order.getValue();
            List<String> promos = order.getPromotions();
            if (promos != null) {
                for (String promoId : promos) {
                    PaymentMethod card = methods.stream()
                            .filter(m -> m.getId().equals(promoId))
                            .findFirst().orElse(null);
                    if (card == null) continue;
                    BigDecimal rate = card.getDiscount().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                    BigDecimal profit = value.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal cost = value.subtract(profit).setScale(2, RoundingMode.HALF_UP);
                    options.add(new AssignmentOption(order, card, AssignmentOption.Type.CARD, profit, cost));
                }
            }
            if (pointsMethod != null) {
                BigDecimal basePct = BigDecimal.valueOf(0.10);
                for (BigDecimal p = basePct; p.compareTo(BigDecimal.ONE) < 0; p = p.add(basePct)) {
                    BigDecimal cost = value.multiply(p).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal profit = value.multiply(basePct).setScale(2, RoundingMode.HALF_UP);
                    options.add(new AssignmentOption(order, pointsMethod, AssignmentOption.Type.PARTIAL_POINTS, profit, cost));
                }
                BigDecimal fullRate = pointsMethod.getDiscount().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                BigDecimal fullProfit = value.multiply(fullRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal fullCost = value.subtract(fullProfit).setScale(2, RoundingMode.HALF_UP);
                options.add(new AssignmentOption(order, pointsMethod, AssignmentOption.Type.FULL_POINTS, fullProfit, fullCost));
            }
        }
        options.sort((a, b) -> b.getProfitDensity().compareTo(a.getProfitDensity()));

        Map<String, AssignmentOption> chosen = new HashMap<>();
        for (AssignmentOption opt : options) {
            String oid = opt.getOrder().getId();
            PaymentMethod m = opt.getPaymentMethod();
            if (chosen.containsKey(oid)) continue;
            if (opt.getCost().compareTo(m.getRemaining()) <= 0) {
                chosen.put(oid, opt);
                m.useAmount(opt.getCost());
            }
        }

        Map<String, BigDecimal> remaining = new HashMap<>();
        for (Order order : orders) {
            AssignmentOption opt = chosen.get(order.getId());
            if (opt == null) {
                remaining.put(order.getId(), order.getValue());
            } else if (opt.getType() == AssignmentOption.Type.CARD || opt.getType() == AssignmentOption.Type.FULL_POINTS) {
                remaining.put(order.getId(), BigDecimal.ZERO);
            } else {
                BigDecimal after = order.getValue().multiply(BigDecimal.valueOf(0.90)).setScale(2, RoundingMode.HALF_UP);
                remaining.put(order.getId(), after.subtract(opt.getCost()).max(BigDecimal.ZERO));
            }
        }

        if (pointsMethod != null) distributePayments(orders, remaining, pointsMethod);
        for (PaymentMethod pm : methods) {
            if ("PUNKTY".equals(pm.getId())) continue;
            distributePayments(orders, remaining, pm);
        }
        return methods;
    }

    private void distributePayments(List<Order> orders, Map<String, BigDecimal> rem, PaymentMethod method) {
        for (Order o : orders) {
            BigDecimal toPay = rem.get(o.getId());
            if (toPay.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal avail = method.getRemaining();
            if (avail.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal use = toPay.min(avail);
            method.useAmount(use);
            rem.put(o.getId(), toPay.subtract(use));
        }
    }
}