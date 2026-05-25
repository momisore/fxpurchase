package com.wex.fxpurchase.infrastructure.treasury;

/**
 * Raised when Treasury API calls fail or responses cannot be parsed.
 */
public class TreasuryApiException extends RuntimeException {

    public TreasuryApiException(String message) {
        super(message);
    }

    public TreasuryApiException(String message, Throwable cause) {
        super(message, cause);
    }
}