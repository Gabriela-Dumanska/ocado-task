package pl.edu.agh;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreedyPaymentOptimizer {

    public List<PaymentMethod> optimizePayments(List<Order> orders, List<PaymentMethod> methods) {
        PaymentMethod pointsMethod = methods.stream()
                .filter(m -> "PUNKTY".equals(m.getId()))
                .findFirst().orElse(null);

        List<AssignmentOption> options = new ArrayList<>();
        for (Order order : orders) {
            BigDecimal value = order.getValue();
            if (order.getPromotions() != null) {
                for (String promoId : order.getPromotions()) {
                    PaymentMethod card = findMethodById(methods, promoId);
                    if (card != null) {
                        BigDecimal rate = card.getDiscount().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                        BigDecimal profit = value.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal cost = value.subtract(profit).setScale(2, RoundingMode.HALF_UP);
                        options.add(new AssignmentOption(order, card, AssignmentOption.Type.CARD, profit, cost));
                    }
                }
            }

            BigDecimal thresholdPct = BigDecimal.valueOf(0.10);
            BigDecimal fullPct       = BigDecimal.valueOf(1.00);
            BigDecimal thresholdCost = value.multiply(thresholdPct).setScale(2, RoundingMode.HALF_UP);

            BigDecimal p = thresholdPct;
            while (p.compareTo(fullPct) < 0) {
                BigDecimal cost = value.multiply(p).setScale(2, RoundingMode.HALF_UP);
                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    options.add(new AssignmentOption(order, pointsMethod,
                            AssignmentOption.Type.PARTIAL_POINTS,
                            thresholdCost,
                            cost));
                }
                p = p.add(thresholdPct);
            }

            BigDecimal pointsRate = pointsMethod.getDiscount().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            BigDecimal fullProfit = value.multiply(pointsRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fullCost   = value.subtract(fullProfit).setScale(2, RoundingMode.HALF_UP);
            options.add(new AssignmentOption(order, pointsMethod,
                    AssignmentOption.Type.FULL_POINTS,
                    fullProfit,
                    fullCost));
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
            } else if (opt.getType() == AssignmentOption.Type.CARD
                    || opt.getType() == AssignmentOption.Type.FULL_POINTS) {
                remaining.put(order.getId(), BigDecimal.ZERO);
            } else {
                BigDecimal afterDiscount = order.getValue()
                        .multiply(BigDecimal.valueOf(0.90))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal left = afterDiscount.subtract(opt.getCost());
                remaining.put(order.getId(), left.max(BigDecimal.ZERO));
            }
        }

        if (pointsMethod != null) {
            distributePayments(orders, remaining, pointsMethod);
        }
        for (PaymentMethod pm : methods) {
            if ("PUNKTY".equals(pm.getId())) continue;
            distributePayments(orders, remaining, pm);
        }

        return methods;
    }

    private void distributePayments(List<Order> orders,
                                    Map<String, BigDecimal> rem,
                                    PaymentMethod method) {
        for (Order o : orders) {
            BigDecimal toPay = rem.get(o.getId());
            if (toPay.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (method.getRemaining().compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal use = toPay.min(method.getRemaining());
            method.useAmount(use);
            rem.put(o.getId(), toPay.subtract(use));
        }
    }

    private PaymentMethod findMethodById(List<PaymentMethod> methods, String id) {
        return methods.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst().orElse(null);
    }
}