import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pl.edu.agh.exception.ErrorException;
import pl.edu.agh.exception.WarningException;
import pl.edu.agh.factory.PaymentMethodFactory;
import pl.edu.agh.model.PaymentMethod;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodFactoryTest {

    @TempDir
    Path tempDir;

    @Test
    void testFromJsonValid() throws Exception {
        String json = "["
                + "{\"id\": \"pm1\", \"discount\": \"0.05\", \"limit\": \"1000\"},"
                + "{\"id\": \"pm2\", \"discount\": \"0\", \"limit\": \"500.50\"}"
                + "]";
        Path file = tempDir.resolve("payment_methods_valid.json");
        Files.writeString(file, json);

        List<PaymentMethod> methods = PaymentMethodFactory.fromJson(file);
        assertEquals(2, methods.size());

        PaymentMethod m1 = methods.get(0);
        assertEquals("pm1", m1.getId());
        assertEquals(new BigDecimal("0.05"), m1.getDiscount());
        assertEquals(new BigDecimal("1000"), m1.getLimit());

        PaymentMethod m2 = methods.get(1);
        assertEquals("pm2", m2.getId());
        assertEquals(new BigDecimal("0"), m2.getDiscount());
        assertEquals(new BigDecimal("500.50"), m2.getLimit());
    }

    @Test
    void testUnreadableFile() {
        Path missing = tempDir.resolve("missing.json");
        IOException ex = assertThrows(IOException.class, () -> PaymentMethodFactory.fromJson(missing));
        assertTrue(ex.getMessage().contains("Cannot read payment methods file"));
    }

    @Test
    void testInvalidJsonThrowsErrorException() throws IOException {
        String invalid = "{ not a valid json }";
        Path file = tempDir.resolve("invalid.json");
        Files.writeString(file, invalid);

        assertThrows(ErrorException.class, () -> PaymentMethodFactory.fromJson(file));
    }

    @Test
    void testRootNotArrayThrowsWarningException() throws IOException {
        String json = "{ \"id\": \"x\" }";
        Path file = tempDir.resolve("not_array.json");
        Files.writeString(file, json);

        WarningException ex = assertThrows(WarningException.class, () -> PaymentMethodFactory.fromJson(file));
        assertEquals("Payment methods JSON root must be an array", ex.getMessage());
    }

    @Test
    void testMissingDiscountOrLimitFieldThrowsWarningException() throws IOException {
        String json = "[{ \"id\": \"a\", \"discount\": \"0.1\" }]";
        Path file = tempDir.resolve("missing_field.json");
        Files.writeString(file, json);

        WarningException ex = assertThrows(WarningException.class, () -> PaymentMethodFactory.fromJson(file));
        assertEquals("Missing 'discount' or 'limit' field in one or more payment methods", ex.getMessage());
    }

    @Test
    void testSkipEntriesWithMissingIdDiscountOrLimit() throws Exception {
        String json = "["
                + "{ \"id\": null, \"discount\": \"0.2\", \"limit\": \"100\" },"
                + "{ \"id\": \"good\", \"discount\": \"0.15\", \"limit\": \"200\" }"
                + "]";
        Path file = tempDir.resolve("skip_missing.json");
        Files.writeString(file, json);

        List<PaymentMethod> methods = PaymentMethodFactory.fromJson(file);
        assertEquals(1, methods.size());
        assertEquals("good", methods.get(0).getId());
    }

    @Test
    void testSkipInvalidNumericFormat() throws Exception {
        String json = "["
                + "{ \"id\": \"bad\", \"discount\": \"NaN\", \"limit\": \"50\" },"
                + "{ \"id\": \"good\", \"discount\": \"0.3\", \"limit\": \"150\" }"
                + "]";
        Path file = tempDir.resolve("skip_invalid_numeric.json");
        Files.writeString(file, json);

        List<PaymentMethod> methods = PaymentMethodFactory.fromJson(file);
        assertEquals(1, methods.size());
        assertEquals("good", methods.get(0).getId());
    }

    @Test
    void testNoValidMethodsThrowsWarningException() throws IOException {
        String json = "["
                + "{ \"id\": \"only\", \"discount\": \"notNumber\", \"limit\": \"invalid\" }"
                + "]";
        Path file = tempDir.resolve("all_invalid.json");
        Files.writeString(file, json);

        WarningException ex = assertThrows(WarningException.class, () -> PaymentMethodFactory.fromJson(file));
        assertTrue(ex.getMessage().startsWith("No valid payment methods loaded"));
    }
}