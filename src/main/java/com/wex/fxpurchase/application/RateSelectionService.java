package com.wex.fxpurchase.application;

import com.wex.fxpurchase.application.exception.NoEligibleRateFoundException;
import com.wex.fxpurchase.domain.ExchangeRate;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Selects the most recent eligible exchange rate using the 6-month rule.
 */
@Service
public class RateSelectionService {

    public ExchangeRate selectMostRecentEligibleRate(
            LocalDate purchaseDate,
            String targetCurrency,
            List<ExchangeRate> rates) {

        LocalDate earliestEligibleDate = purchaseDate.minusMonths(6);

        return rates.stream()
                .filter(rate -> rate.rateDate() != null)
                .filter(rate -> !rate.rateDate().isAfter(purchaseDate))
                .filter(rate -> !rate.rateDate().isBefore(earliestEligibleDate))
                .max(Comparator.comparing(ExchangeRate::rateDate))
                .orElseThrow(() -> new NoEligibleRateFoundException(targetCurrency));
    }
}