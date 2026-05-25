package com.wex.fxpurchase.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a purchase transaction.
 * Contains input fields validated before processing the create transaction endpoint.
 */

public class CreatePurchaseTransactionRequest {

    @NotBlank(message = "description is required")
    @Size(max = 50, message = "description must be at most 50 characters")
    private String description;

    @NotNull(message = "transactionDate is required")
    private LocalDate transactionDate;

    @NotNull(message = "usdAmount is required")
    @Positive(message = "usdAmount must be positive")
    private BigDecimal usdAmount;

    public CreatePurchaseTransactionRequest() {
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
}