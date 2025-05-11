import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pl.edu.agh.exception.ErrorException;
import pl.edu.agh.exception.WarningException;
import pl.edu.agh.factory.OrderFactory;
import pl.edu.agh.model.Order;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderFactoryTest {

    @TempDir
    Path tempDir;

    @Test
    void testFromJsonValid() throws Exception {
        String json = "["
                + "{\"id\": \"order1\", \"value\": \"100.50\", \"promotions\": [\"PROMO1\", \"PROMO2\"]},"
                + "{\"id\": \"order2\", \"value\": \"200\", \"promotions\": []}"
                + "]";
        Path file = tempDir.resolve("orders_valid.json");
        Files.writeString(file, json);

        List<Order> orders = OrderFactory.fromJson(file);

        assertEquals(2, orders.size());

        Order o1 = orders.get(0);
        assertEquals("order1", o1.getId());
        assertEquals(new BigDecimal("100.50"), o1.getValue());
        assertNotNull(o1.getPromotions());
        assertEquals(List.of("PROMO1", "PROMO2"), o1.getPromotions());

        Order o2 = orders.get(1);
        assertEquals("order2", o2.getId());
        assertEquals(new BigDecimal("200"), o2.getValue());
        // empty promotions array yields null in Order
        assertNull(o2.getPromotions());
    }

    @Test
    void testUnreadableFile() {
        Path missing = tempDir.resolve("missing.json");
        IOException ex = assertThrows(IOException.class, () -> OrderFactory.fromJson(missing));
        assertTrue(ex.getMessage().contains("Cannot read orders file"));
    }

    @Test
    void testInvalidJsonThrowsErrorException() throws IOException {
        String invalid = "{ invalid json }";
        Path file = tempDir.resolve("orders_invalid.json");
        Files.writeString(file, invalid);

        assertThrows(ErrorException.class, () -> OrderFactory.fromJson(file));
    }

    @Test
    void testRootNotArrayThrowsWarningException() throws IOException {
        String json = "{ \"id\": \"x\" }";
        Path file = tempDir.resolve("root_not_array.json");
        Files.writeString(file, json);

        WarningException ex = assertThrows(WarningException.class, () -> OrderFactory.fromJson(file));
        assertEquals("Orders JSON root must be an array", ex.getMessage());
    }

    @Test
    void testMissingPromotionsFieldThrowsWarningException() throws IOException {
        String json = "[{ \"id\": \"1\", \"value\": \"10\" }]";
        Path file = tempDir.resolve("missing_promotions.json");
        Files.writeString(file, json);

        WarningException ex = assertThrows(WarningException.class, () -> OrderFactory.fromJson(file));
        assertEquals("Missing 'promotions' field in one or more orders", ex.getMessage());
    }

    @Test
    void testSkipEntriesWithMissingIdOrValue() throws Exception {
        String json = "["
                + "{ \"id\": null, \"value\": \"50\", \"promotions\": [] },"
                + "{ \"id\": \"good\", \"value\": \"75\", \"promotions\": [] }"
                + "]";
        Path file = tempDir.resolve("skip_missing_id.json");
        Files.writeString(file, json);

        List<Order> orders = OrderFactory.fromJson(file);
        assertEquals(1, orders.size());
        assertEquals("good", orders.get(0).getId());
    }

    @Test
    void testSkipInvalidValueFormat() throws Exception {
        String json = "["
                + "{ \"id\": \"badValue\", \"value\": \"notANumber\", \"promotions\": [] },"
                + "{ \"id\": \"goodValue\", \"value\": \"123.45\", \"promotions\": [] }"
                + "]";
        Path file = tempDir.resolve("skip_invalid_value.json");
        Files.writeString(file, json);

        List<Order> orders = OrderFactory.fromJson(file);
        assertEquals(1, orders.size());
        assertEquals("goodValue", orders.get(0).getId());
    }

    @Test
    void testNonArrayPromotionsResultsInNull() throws Exception {
        String json = "["
                + "{ \"id\": \"orderX\", \"value\": \"10\", \"promotions\": \"notAnArray\" }"
                + "]";
        Path file = tempDir.resolve("promotions_not_array.json");
        Files.writeString(file, json);

        List<Order> orders = OrderFactory.fromJson(file);
        assertEquals(1, orders.size());
        assertNull(orders.get(0).getPromotions());
    }

    @Test
    void testIgnoreNonTextualPromotionElements() throws Exception {
        String json = "["
                + "{ \"id\": \"orderY\", \"value\": \"20\", \"promotions\": [\"PROMO\", 123, null] }"
                + "]";
        Path file = tempDir.resolve("promotions_mixed.json");
        Files.writeString(file, json);

        List<Order> orders = OrderFactory.fromJson(file);
        assertEquals(1, orders.size());
        assertEquals(List.of("PROMO"), orders.get(0).getPromotions());
    }
}