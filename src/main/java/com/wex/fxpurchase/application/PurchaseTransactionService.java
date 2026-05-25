package com.wex.fxpurchase.application;

import com.wex.fxpurchase.api.dto.ConvertedTransactionResponse;
import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionRequest;
import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionResponse;
import com.wex.fxpurchase.application.exception.TransactionNotFoundException;
import com.wex.fxpurchase.domain.ExchangeRate;
import com.wex.fxpurchase.domain.PurchaseTransaction;
import com.wex.fxpurchase.infrastructure.PurchaseTransactionRepository;
import com.wex.fxpurchase.infrastructure.treasury.TreasuryApiClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PurchaseTransactionService {

    private final PurchaseTransactionRepository repository;
    private final TreasuryApiClient treasuryApiClient;
    private final RateSelectionService rateSelectionService;

    public PurchaseTransactionService(
            PurchaseTransactionRepository repository,
            TreasuryApiClient treasuryApiClient,
            RateSelectionService rateSelectionService) {
        this.repository = repository;
        this.treasuryApiClient = treasuryApiClient;
        this.rateSelectionService = rateSelectionService;
    }

    public CreatePurchaseTransactionResponse create(CreatePurchaseTransactionRequest request) {
        PurchaseTransaction entity = new PurchaseTransaction();
        entity.setDescription(request.getDescription().trim());
        entity.setTransactionDate(request.getTransactionDate());
        entity.setUsdAmount(request.getUsdAmount().setScale(2, RoundingMode.HALF_UP));

        PurchaseTransaction saved = repository.save(entity);

        CreatePurchaseTransactionResponse response = new CreatePurchaseTransactionResponse();
        response.setId(saved.getId());
        response.setDescription(saved.getDescription());
        response.setTransactionDate(saved.getTransactionDate());
        response.setUsdAmount(saved.getUsdAmount());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());
        return response;
    }

    public List<CreatePurchaseTransactionResponse> getAllTransactions() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PurchaseTransaction getById(Long id) {
        Long safeId = Objects.requireNonNull(id, "id must not be null");
        return repository.findById(safeId)
                .orElseThrow(() -> new TransactionNotFoundException(safeId));
    }

    public ConvertedTransactionResponse getConvertedTransaction(Long id, String targetCurrency) {
        PurchaseTransaction transaction = getById(id);
        String normalizedCurrency = targetCurrency.trim().toUpperCase(Locale.ROOT);

        List<ExchangeRate> rates = treasuryApiClient.fetchRates(normalizedCurrency);
        ExchangeRate selectedRate = rateSelectionService.selectMostRecentEligibleRate(
                transaction.getTransactionDate(),
                normalizedCurrency,
                rates);

        BigDecimal convertedAmount = transaction.getUsdAmount()
                .multiply(selectedRate.rate())
                .setScale(2, RoundingMode.HALF_UP);

        ConvertedTransactionResponse response = new ConvertedTransactionResponse();
        response.setId(transaction.getId());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setUsdAmount(transaction.getUsdAmount());
        response.setExchangeRate(selectedRate.rate());
        response.setConvertedAmount(convertedAmount);
        response.setTargetCurrency(normalizedCurrency);
        response.setRateDateUsed(selectedRate.rateDate());
        return response;
    }

    private CreatePurchaseTransactionResponse toResponse(PurchaseTransaction saved) {
        CreatePurchaseTransactionResponse response = new CreatePurchaseTransactionResponse();
        response.setId(saved.getId());
        response.setDescription(saved.getDescription());
        response.setTransactionDate(saved.getTransactionDate());
        response.setUsdAmount(saved.getUsdAmount());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());
        return response;
    }
}