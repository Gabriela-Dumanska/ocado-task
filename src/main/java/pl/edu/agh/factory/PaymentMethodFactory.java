package pl.edu.agh.factory;

import pl.edu.agh.model.PaymentMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodFactory {
    public static List<PaymentMethod> fromJson(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(path.toFile());
        List<PaymentMethod> methods = new ArrayList<>();
        for (JsonNode node : root) {
            String id = node.get("id").asText();
            BigDecimal discount = new BigDecimal(node.get("discount").asText());
            BigDecimal limit = new BigDecimal(node.get("limit").asText());
            methods.add(new PaymentMethod(id, discount, limit));
        }
        return methods;
    }
}