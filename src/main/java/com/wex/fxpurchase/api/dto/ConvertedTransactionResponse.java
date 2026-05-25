package com.wex.fxpurchase.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response payload for converted transaction retrieval.
 */
public class ConvertedTransactionResponse {

    private Long id;
    private String description;
    private LocalDate transactionDate;
    private BigDecimal usdAmount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String targetCurrency;
    private LocalDate rateDateUsed;

    public ConvertedTransactionResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getUsdAmount() {
        return usdAmount;
    }

    public void setUsdAmount(BigDecimal usdAmount) {
        this.usdAmount = usdAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public LocalDate getRateDateUsed() {
        return rateDateUsed;
    }

    public void setRateDateUsed(LocalDate rateDateUsed) {
        this.rateDateUsed = rateDateUsed;
    }
}