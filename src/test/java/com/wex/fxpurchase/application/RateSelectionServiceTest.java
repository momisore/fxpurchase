package com.wex.fxpurchase.application;

import com.wex.fxpurchase.application.exception.NoEligibleRateFoundException;
import com.wex.fxpurchase.domain.ExchangeRate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateSelectionServiceTest {

    private final RateSelectionService service = new RateSelectionService();

    @Test
    void selectMostRecentEligibleRate_shouldPickMostRecentWithinWindow() {
        LocalDate purchaseDate = LocalDate.of(2026, 5, 24);
        List<ExchangeRate> rates = List.of(
                new ExchangeRate("EUR", LocalDate.of(2025, 12, 1), new BigDecimal("0.90")),
                new ExchangeRate("EUR", LocalDate.of(2026, 3, 1), new BigDecimal("0.91")),
                new ExchangeRate("EUR", LocalDate.of(2026, 5, 20), new BigDecimal("0.92"))
        );

        ExchangeRate selected = service.selectMostRecentEligibleRate(purchaseDate, "EUR", rates);

        assertEquals(LocalDate.of(2026, 5, 20), selected.rateDate());
        assertEquals(0, new BigDecimal("0.92").compareTo(selected.rate()));
    }

    @Test
    void selectMostRecentEligibleRate_shouldExcludeFutureRates() {
        LocalDate purchaseDate = LocalDate.of(2026, 5, 24);
        List<ExchangeRate> rates = List.of(
                new ExchangeRate("EUR", LocalDate.of(2026, 5, 25), new BigDecimal("0.99")),
                new ExchangeRate("EUR", LocalDate.of(2026, 5, 10), new BigDecimal("0.90"))
        );

        ExchangeRate selected = service.selectMostRecentEligibleRate(purchaseDate, "EUR", rates);

        assertEquals(LocalDate.of(2026, 5, 10), selected.rateDate());
    }

    @Test
    void selectMostRecentEligibleRate_shouldExcludeRatesOlderThanSixMonths() {
        LocalDate purchaseDate = LocalDate.of(2026, 5, 24);
        List<ExchangeRate> rates = List.of(
                new ExchangeRate("EUR", LocalDate.of(2025, 11, 23), new BigDecimal("0.80")),
                new ExchangeRate("EUR", LocalDate.of(2025, 11, 24), new BigDecimal("0.81"))
        );

        ExchangeRate selected = service.selectMostRecentEligibleRate(purchaseDate, "EUR", rates);

        assertEquals(LocalDate.of(2025, 11, 24), selected.rateDate());
        assertEquals(0, new BigDecimal("0.81").compareTo(selected.rate()));
    }

    @Test
    void selectMostRecentEligibleRate_shouldThrowWhenNoEligibleRateExists() {
        LocalDate purchaseDate = LocalDate.of(2026, 5, 24);
        List<ExchangeRate> rates = List.of(
                new ExchangeRate("EUR", LocalDate.of(2026, 5, 25), new BigDecimal("0.95")),
                new ExchangeRate("EUR", LocalDate.of(2025, 10, 1), new BigDecimal("0.70"))
        );

        NoEligibleRateFoundException ex = assertThrows(
                NoEligibleRateFoundException.class,
                () -> service.selectMostRecentEligibleRate(purchaseDate, "EUR", rates)
        );

        assertEquals("Purchase cannot be converted to the target currency because no eligible exchange rate was found within 6 months on or before the purchase date: EUR", ex.getMessage());
    }
}