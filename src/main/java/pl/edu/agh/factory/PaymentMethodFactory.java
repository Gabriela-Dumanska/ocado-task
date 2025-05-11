package pl.edu.agh.factory;

import pl.edu.agh.model.PaymentMethod;
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

public class PaymentMethodFactory {
    private static final Logger logger = Logger.getLogger(PaymentMethodFactory.class.getName());

    public static List<PaymentMethod> fromJson(Path path)
            throws IOException, WarningException, ErrorException {
        if (!Files.isReadable(path)) {
            throw new IOException("Cannot read payment methods file: " + path);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(path.toFile());
        } catch (JsonParseException | JsonMappingException e) {
            throw new ErrorException("Invalid JSON in payment methods file: " + path, e);
        } catch (IOException e) {
            throw new IOException("Cannot read payment methods file: " + path, e);
        }

        if (!root.isArray()) {
            throw new WarningException("Payment methods JSON root must be an array");
        }

        for (JsonNode node : root) {
            if (!node.has("discount") || !node.has("limit")) {
                throw new WarningException("Missing 'discount' or 'limit' field in one or more payment methods");
            }
        }

        List<PaymentMethod> methods = new ArrayList<>();
        for (JsonNode node : root) {
            if (!node.hasNonNull("id")
                    || !node.hasNonNull("discount")
                    || !node.hasNonNull("limit")) {
                logger.warning("Skipping payment method with missing id/discount/limit: " + node);
                continue;
            }

            String id = node.get("id").asText();
            BigDecimal discount;
            BigDecimal limit;
            try {
                discount = new BigDecimal(node.get("discount").asText());
                limit    = new BigDecimal(node.get("limit").asText());
            } catch (NumberFormatException ex) {
                logger.warning("Invalid numeric field for payment method '" + id + "': " + node);
                continue;
            }

            methods.add(new PaymentMethod(id, discount, limit));
        }

        if (methods.isEmpty()) {
            throw new WarningException("No valid payment methods loaded from " + path);
        }

        return methods;
    }
}
