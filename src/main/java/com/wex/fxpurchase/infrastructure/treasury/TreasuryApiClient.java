package com.wex.fxpurchase.infrastructure.treasury;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.fxpurchase.config.TreasuryApiProperties;
import com.wex.fxpurchase.domain.ExchangeRate;
import java.math.BigDecimal;
import java.util.Currency;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client wrapper for Treasury Reporting Rates API.
 */
@Component
public class TreasuryApiClient {

    private final TreasuryApiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public TreasuryApiClient(TreasuryApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public List<ExchangeRate> fetchRates(String targetCurrency) {
        String normalizedCurrency = normalizeCurrency(targetCurrency);
        TreasuryLookup lookup = resolveTreasuryLookup(normalizedCurrency);
        URI uri = buildRatesUri(lookup.fieldName(), lookup.fieldValue());

        int attempts = Math.max(0, properties.getMaxRetries()) + 1;
        long backoffMs = Math.max(0, properties.getRetryBackoffMs());

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                String responseBody = restClient.get()
                        .uri(Objects.requireNonNull(uri, "uri must not be null"))
                        .retrieve()
                        .body(String.class);

                if (responseBody == null || responseBody.isBlank()) {
                    throw new TreasuryApiException("Treasury API returned an empty response");
                }

                return mapRates(responseBody, normalizedCurrency);
            } catch (Exception ex) {
                if (attempt == attempts) {
                    throw new TreasuryApiException("Failed to retrieve Treasury rates", ex);
                }
                sleep(backoffMs);
            }
        }

        throw new TreasuryApiException("Unexpected Treasury API client state");
    }

    private URI buildRatesUri(String fieldName, String fieldValue) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new TreasuryApiException("Treasury API base URL is not configured");
        }

        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/v1/accounting/od/rates_of_exchange")
                .queryParam("fields", "currency,exchange_rate,record_date")
                .queryParam("filter", fieldName + ":in:(" + fieldValue + ")")
                .queryParam("sort", "-record_date")
                .queryParam("page[size]", "1000")
                .build()
                .encode()
                .toUri();
    }

    private List<ExchangeRate> mapRates(String responseBody, String targetCurrency) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.path("data");

            List<ExchangeRate> rates = new ArrayList<>();
            if (!data.isArray()) {
                return rates;
            }

            for (JsonNode node : data) {
                String rateValue = node.path("exchange_rate").asText(null);
                String recordDate = node.path("record_date").asText(null);

                if (rateValue == null || recordDate == null) {
                    continue;
                }

                BigDecimal rate = parseRate(rateValue);
                LocalDate rateDate = LocalDate.parse(recordDate);

                rates.add(new ExchangeRate(targetCurrency, rateDate, rate));
            }

            return rates;
        } catch (Exception ex) {
            throw new TreasuryApiException("Failed to parse Treasury API response", ex);
        }
    }

    private BigDecimal parseRate(String rawRate) {
        String normalized = rawRate.replace(",", "").trim();
        return new BigDecimal(normalized);
    }

    private String normalizeCurrency(String targetCurrency) {
        if (targetCurrency == null || targetCurrency.isBlank()) {
            throw new TreasuryApiException("Target currency is required");
        }
        return targetCurrency.trim().toUpperCase(Locale.ROOT);
    }

    private TreasuryLookup resolveTreasuryLookup(String normalizedCurrency) {
        if (normalizedCurrency.contains("-")) {
            return new TreasuryLookup("country_currency_desc", normalizedCurrency);
        }

        Currency resolvedCurrency = null;

        if (normalizedCurrency.length() == 3) {
            try {
                resolvedCurrency = Currency.getInstance(normalizedCurrency);
            } catch (IllegalArgumentException ignored) {
                resolvedCurrency = null;
            }
        }

        if (resolvedCurrency == null) {
            String normalizedInput = normalizedCurrency.toLowerCase(Locale.ROOT);
            resolvedCurrency = Currency.getAvailableCurrencies().stream()
                    .filter(currency -> matchesCurrencyAlias(currency, normalizedInput))
                    .findFirst()
                    .orElse(null);
        }

        if (resolvedCurrency == null) {
            return new TreasuryLookup("currency", normalizedCurrency);
        }

        return new TreasuryLookup("currency", toTreasuryCurrencyLabel(resolvedCurrency));
    }

    private boolean matchesCurrencyAlias(Currency currency, String normalizedInput) {
        String displayName = currency.getDisplayName(Locale.US).toLowerCase(Locale.ROOT);
        String code = currency.getCurrencyCode().toLowerCase(Locale.ROOT);
        String treasuryLabel = toTreasuryCurrencyLabel(currency).toLowerCase(Locale.ROOT);

        return code.equals(normalizedInput)
                || displayName.equals(normalizedInput)
                || displayName.endsWith(" " + normalizedInput)
                || treasuryLabel.equals(normalizedInput);
    }

    private String toTreasuryCurrencyLabel(Currency currency) {
        String displayName = currency.getDisplayName(Locale.US).trim();
        int lastSpaceIndex = displayName.lastIndexOf(' ');
        if (lastSpaceIndex < 0) {
            return displayName;
        }
        return displayName.substring(lastSpaceIndex + 1);
    }

    private record TreasuryLookup(String fieldName, String fieldValue) {
    }

    private void sleep(long backoffMs) {
        if (backoffMs <= 0) {
            return;
        }
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new TreasuryApiException("Retry sleep interrupted", ie);
        }
    }
}