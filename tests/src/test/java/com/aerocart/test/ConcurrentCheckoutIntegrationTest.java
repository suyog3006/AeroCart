package com.aerocart.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentCheckoutIntegrationTest {

    private static final String GATEWAY_URL = "http://localhost:8080/api/v1/orders/checkout";
    private static final int CONCURRENT_REQUESTS = 10000;
    private static final int THREAD_POOL_SIZE = 100;
    private static final int INITIAL_STOCK = 500;

    @Test
    @DisplayName("Simulate 10,000 Concurrent Checkout Requests with Zero Overselling")
    public void testHighVolumeConcurrentCheckout() throws InterruptedException {
        System.out.println("Starting Concurrency Integration Test: " + CONCURRENT_REQUESTS + " requests...");

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(CONCURRENT_REQUESTS);

        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final long userId = (i % 1000) + 1;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    int statusCode = sendCheckoutRequest(userId, "PROD-APPLE-MACBOOK", 1, 1499.99);
                    if (statusCode == 200) {
                        successfulOrders.incrementAndGet();
                    } else {
                        failedOrders.incrementAndGet();
                    }
                } catch (Exception e) {
                    failedOrders.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // Fire all 10,000 requests simultaneously

        boolean completedInTime = finishLatch.await(60, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        executorService.shutdown();

        System.out.println("=== HIGH VOLUME CONCURRENCY TEST COMPLETED ===");
        System.out.println("Execution Time: " + duration + " ms");
        System.out.println("Successful Orders: " + successfulOrders.get());
        System.out.println("Failed/Rejected Orders: " + failedOrders.get());
        System.out.println("Total Handled: " + (successfulOrders.get() + failedOrders.get()));

        assertTrue(completedInTime, "Test timed out before processing 10,000 concurrent checkouts");
        // Verify that stock is never oversold beyond initial stock limit
        assertTrue(successfulOrders.get() <= INITIAL_STOCK,
                "RACE CONDITION DETECTED! Successful orders (" + successfulOrders.get() + ") exceeded initial stock (" + INITIAL_STOCK + ")");
        assertEquals(CONCURRENT_REQUESTS, successfulOrders.get() + failedOrders.get(),
                "All 10,000 requests must be accounted for");
    }

    private int sendCheckoutRequest(long userId, String productId, int quantity, double amount) throws Exception {
        URL url = new URL(GATEWAY_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);

        String jsonInput = String.format(
                "{\"userId\":%d,\"productId\":\"%s\",\"quantity\":%d,\"amount\":%.2f}",
                userId, productId, quantity, amount
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return conn.getResponseCode();
    }
}
