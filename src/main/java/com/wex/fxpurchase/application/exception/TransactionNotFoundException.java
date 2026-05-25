package com.wex.fxpurchase.application.exception;

/**
 * Thrown when a purchase transaction cannot be found by id.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("Transaction not found for id: " + id);
    }
}