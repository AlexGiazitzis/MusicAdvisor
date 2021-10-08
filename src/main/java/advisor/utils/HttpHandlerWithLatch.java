package advisor.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * Custom implementation of {@link com.sun.net.httpserver.HttpHandler} that introduces
 * a {@link java.util.concurrent.CountDownLatch} field that's used to control the program flow,
 * a {@link java.lang.String} field that enables the ability to retrieve the
 * incoming GET request query parameters/
 *
 * @author Alex Giazitzis
 */
public class HttpHandlerWithLatch implements HttpHandler {
    private final CountDownLatch latch;
    public static String query;

    public HttpHandlerWithLatch(final CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        query = exchange.getRequestURI().getQuery();
        String response = "Got the code. Return back to your program.";
        if (query == null || query.contains("error=")) {
            response = "Authorization code not found. Try again.";
            query = null;
        }
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
        if (query != null) {
            latch.countDown();
        }
    }
}
