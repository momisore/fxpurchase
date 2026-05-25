package com.wex.fxpurchase.infrastructure.treasury;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wex.fxpurchase.config.TreasuryApiProperties;
import com.wex.fxpurchase.domain.ExchangeRate;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreasuryApiClientTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchRates_shouldParseTreasuryResponse() {
                AtomicReference<String> rawQuery = new AtomicReference<>();
        String json = """
                {
                  "data": [
                    {
                      "country_currency_desc": "EUR",
                      "exchange_rate": "0.92",
                      "record_date": "2026-05-20"
                    },
                    {
                      "country_currency_desc": "EUR",
                      "exchange_rate": "0.90",
                      "record_date": "2026-04-10"
                    }
                  ]
                }
                """;

            server.createContext("/v1/accounting/od/rates_of_exchange", new FixedResponseHandler(200, json, rawQuery));
        server.start();

        TreasuryApiClient client = new TreasuryApiClient(buildProps(), new ObjectMapper());
        List<ExchangeRate> rates = client.fetchRates("EUR");

        assertEquals(2, rates.size());
        assertEquals("EUR", rates.get(0).currency());
        assertEquals(LocalDate.of(2026, 5, 20), rates.get(0).rateDate());
        assertEquals(0, new BigDecimal("0.92").compareTo(rates.get(0).rate()));
        assertTrue(rawQuery.get().contains("currency:in:(Euro)"));
            assertTrue(rawQuery.get().contains("page%5Bsize%5D=1000"));
    }

        @Test
        void fetchRates_shouldUseCountryCurrencyDescriptionWhenProvided() {
                AtomicReference<String> rawQuery = new AtomicReference<>();
                String json = """
                                {
                                    "data": [
                                        {
                                            "country_currency_desc": "Afghanistan-Afghani",
                                            "exchange_rate": "64.77",
                                            "record_date": "2026-03-31"
                                        }
                                    ]
                                }
                                """;

                server.createContext("/v1/accounting/od/rates_of_exchange", new FixedResponseHandler(200, json, rawQuery));
                server.start();

                TreasuryApiClient client = new TreasuryApiClient(buildProps(), new ObjectMapper());
                List<ExchangeRate> rates = client.fetchRates("Afghanistan-Afghani");

                assertEquals(1, rates.size());
                assertTrue(rawQuery.get().contains("country_currency_desc:in:(AFGHANISTAN-AFGHANI)"));
        }

        @Test
        void fetchRates_shouldResolvePlainCurrencyName() {
                AtomicReference<String> rawQuery = new AtomicReference<>();
                String json = """
                                {
                                    "data": [
                                        {
                                            "country_currency_desc": "Brazil-Real",
                                            "exchange_rate": "5.254",
                                            "record_date": "2026-03-31"
                                        }
                                    ]
                                }
                                """;

                server.createContext("/v1/accounting/od/rates_of_exchange", new FixedResponseHandler(200, json, rawQuery));
                server.start();

                TreasuryApiClient client = new TreasuryApiClient(buildProps(), new ObjectMapper());
                List<ExchangeRate> rates = client.fetchRates("Real");

                assertEquals(1, rates.size());
                assertTrue(rawQuery.get().contains("currency:in:(Real)"));
        }

    @Test
    void fetchRates_shouldThrowOnEmptyBody() {
        server.createContext("/v1/accounting/od/rates_of_exchange", new FixedResponseHandler(200, "", new AtomicReference<>()));
        server.start();

        TreasuryApiClient client = new TreasuryApiClient(buildProps(), new ObjectMapper());

        assertThrows(TreasuryApiException.class, () -> client.fetchRates("EUR"));
    }

    @Test
    void fetchRates_shouldThrowWhenCurrencyBlank() {
        TreasuryApiClient client = new TreasuryApiClient(buildProps(), new ObjectMapper());
        assertThrows(TreasuryApiException.class, () -> client.fetchRates(" "));
    }

        @Test
        void fetchRates_shouldResolveCommonCurrencyNameAliases() {
                AtomicReference<String> rawQuery = new AtomicReference<>();
                String json = """
                                {
                                    "data": [
                                        {
                                            "country_currency_desc": "Nigeria-Naira",
                                            "exchange_rate": "1375.0",
                                            "record_date": "2026-03-31"
                                        }
                                    ]
                                }
                                """;

                server.createContext("/v1/accounting/od/rates_of_exchange", new FixedResponseHandler(200, json, rawQuery));
                server.start();

                TreasuryApiClient client = new TreasuryApiClient(buildProps(), new ObjectMapper());
                List<ExchangeRate> rates = client.fetchRates("NAIRA");

                assertEquals(1, rates.size());
                assertTrue(rawQuery.get().contains("currency:in:(Naira)"));
        }

    private TreasuryApiProperties buildProps() {
        TreasuryApiProperties props = new TreasuryApiProperties();
        props.setBaseUrl(baseUrl);
        props.setConnectTimeoutMs(2000);
        props.setReadTimeoutMs(2000);
        props.setMaxRetries(0);
        props.setRetryBackoffMs(0);
        return props;
    }

    private static class FixedResponseHandler implements HttpHandler {
        private final int status;
        private final String body;
        private final AtomicReference<String> rawQuery;

        FixedResponseHandler(int status, String body, AtomicReference<String> rawQuery) {
            this.status = status;
            this.body = body;
            this.rawQuery = rawQuery;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            byte[] bytes = body.getBytes();
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}