package pl.edu.agh.factory;

import pl.edu.agh.model.Order;
import pl.edu.agh.exception.WarningException;
import pl.edu.agh.exception.ErrorException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OrderFactory {
    private static final Logger logger = Logger.getLogger(OrderFactory.class.getName());

    public static List<Order> fromJson(Path path) throws IOException, WarningException, ErrorException {
        if (!Files.isReadable(path)) {
            throw new IOException("Cannot read orders file: " + path);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(path.toFile());
        } catch (JsonParseException | JsonMappingException e) {
            throw new ErrorException("Invalid JSON in orders file: " + path, e);
        } catch (IOException e) {
            throw new IOException("Cannot read orders file: " + path, e);
        }

        if (!root.isArray()) {
            throw new WarningException("Orders JSON root must be an array");
        }

        List<Order> orders = new ArrayList<>();
        for (JsonNode node : root) {
            if (!node.hasNonNull("id") || !node.hasNonNull("value")) {
                logger.warning("Skipping order entry with missing id or value: " + node);
                continue;
            }

            String id = node.get("id").asText();
            BigDecimal value;
            try {
                value = new BigDecimal(node.get("value").asText());
            } catch (NumberFormatException ex) {
                logger.warning("Invalid value for order '" + id + "': " + node.get("value").asText());
                continue;
            }

            List<String> promotions = new ArrayList<>();
            JsonNode promos = node.get("promotions");
            if (promos != null && promos.isArray()) {
                for (JsonNode p : promos) {
                    if (p.isTextual()) {
                        promotions.add(p.asText());
                    } else {
                        logger.warning("Ignoring non-text promotion for order '" + id + "': " + p);
                    }
                }
            } else if (promos != null) {
                logger.warning("Field 'promotions' is not an array for order '" + id + "': " + promos);
            }

            orders.add(new Order(id, value, promotions.isEmpty() ? null : promotions));
        }

        return orders;
    }
}
