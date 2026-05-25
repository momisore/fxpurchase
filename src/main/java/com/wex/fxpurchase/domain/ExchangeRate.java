package com.wex.fxpurchase.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Internal model representing a Treasury exchange rate entry.
 */
public record ExchangeRate(
        String currency,
        LocalDate rateDate,
        BigDecimal rate
) {
}