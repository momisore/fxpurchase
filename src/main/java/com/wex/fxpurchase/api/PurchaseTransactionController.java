package com.wex.fxpurchase.api;

import com.wex.fxpurchase.api.dto.ConvertedTransactionResponse;
import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionRequest;
import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionResponse;
import com.wex.fxpurchase.application.PurchaseTransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes HTTP endpoints for purchase transactions.
 */
@RestController
@RequestMapping("/api/transactions")
public class PurchaseTransactionController {

    private final PurchaseTransactionService service;

    public PurchaseTransactionController(PurchaseTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreatePurchaseTransactionResponse> createTransaction(
            @Valid @RequestBody CreatePurchaseTransactionRequest request) {

        CreatePurchaseTransactionResponse response = service.create(request);
        String location = "/api/transactions/" + response.getId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", location)
                .body(response);
    }

    @GetMapping("/{id}/converted")
    public ResponseEntity<ConvertedTransactionResponse> getConvertedTransaction(
            @PathVariable Long id,
            @RequestParam String targetCurrency) {

        ConvertedTransactionResponse response = service.getConvertedTransaction(id, targetCurrency);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CreatePurchaseTransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(service.getAllTransactions());
    }
}