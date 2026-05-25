package com.wex.fxpurchase.application.exception;

/**
 * Raised when no Treasury exchange rate satisfies the 6-month selection rule.
 */
public class NoEligibleRateFoundException extends RuntimeException {

    public NoEligibleRateFoundException(String targetCurrency) {
        super("Purchase cannot be converted to the target currency because no eligible exchange rate was found within 6 months on or before the purchase date: " + targetCurrency);
    }
}