package pl.edu.agh.factory;

import pl.edu.agh.model.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OrderFactory {
    public static List<Order> fromJson(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(path.toFile());
        List<Order> orders = new ArrayList<>();
        for (JsonNode node : root) {
            String id = node.get("id").asText();
            BigDecimal value = new BigDecimal(node.get("value").asText());
            List<String> promotions = null;
            JsonNode promos = node.get("promotions");
            if (promos != null && promos.isArray()) {
                promotions = new ArrayList<>();
                for (JsonNode p : promos) promotions.add(p.asText());
            }
            orders.add(new Order(id, value, promotions));
        }
        return orders;
    }
}