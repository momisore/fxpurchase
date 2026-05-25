package com.wex.fxpurchase;

import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionRequest;
import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionResponse;
import com.wex.fxpurchase.application.PurchaseTransactionService;
import com.wex.fxpurchase.application.RateSelectionService;
import com.wex.fxpurchase.application.exception.TransactionNotFoundException;
import com.wex.fxpurchase.domain.PurchaseTransaction;
import com.wex.fxpurchase.infrastructure.PurchaseTransactionRepository;
import com.wex.fxpurchase.infrastructure.treasury.TreasuryApiClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class PurchaseTransactionServiceTest {

    @Mock
    private PurchaseTransactionRepository repository;

    @Mock
    private TreasuryApiClient treasuryApiClient;

    @Mock
    private RateSelectionService rateSelectionService;

    @InjectMocks
    private PurchaseTransactionService service;

    @Test
    void create_shouldNormalizeAmountTrimDescription_andReturnResponse() {
        CreatePurchaseTransactionRequest request = new CreatePurchaseTransactionRequest();
        request.setDescription("  Office supplies  ");
        request.setTransactionDate(LocalDate.of(2026, 5, 24));
        request.setUsdAmount(new BigDecimal("123.456"));

        when(repository.save(any(PurchaseTransaction.class))).thenAnswer(invocation -> {
            PurchaseTransaction input = invocation.getArgument(0);
            PurchaseTransaction saved = new PurchaseTransaction();
            saved.setId(1L);
            saved.setDescription(input.getDescription());
            saved.setTransactionDate(input.getTransactionDate());
            saved.setUsdAmount(input.getUsdAmount());
            saved.setCreatedAt(LocalDateTime.of(2026, 5, 24, 10, 0, 0));
            saved.setUpdatedAt(LocalDateTime.of(2026, 5, 24, 10, 0, 0));
            return saved;
        });

        CreatePurchaseTransactionResponse response = service.create(request);

        ArgumentCaptor<PurchaseTransaction> captor = ArgumentCaptor.forClass(PurchaseTransaction.class);
        verify(repository).save(captor.capture());
        PurchaseTransaction persisted = captor.getValue();

        assertEquals("Office supplies", persisted.getDescription());
        assertEquals(0, new BigDecimal("123.46").compareTo(persisted.getUsdAmount()));

        assertEquals(1L, response.getId());
        assertEquals("Office supplies", response.getDescription());
        assertEquals(LocalDate.of(2026, 5, 24), response.getTransactionDate());
        assertEquals(0, new BigDecimal("123.46").compareTo(response.getUsdAmount()));
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void getAllTransactions_shouldMapAllRepositoryResults() {
        PurchaseTransaction first = new PurchaseTransaction();
        first.setId(1L);
        first.setDescription("Laptop");
        first.setTransactionDate(LocalDate.of(2026, 5, 24));
        first.setUsdAmount(new BigDecimal("1500.00"));

        PurchaseTransaction second = new PurchaseTransaction();
        second.setId(2L);
        second.setDescription("Printer");
        second.setTransactionDate(LocalDate.of(2026, 5, 23));
        second.setUsdAmount(new BigDecimal("500.00"));

        when(repository.findAll()).thenReturn(List.of(first, second));

        List<CreatePurchaseTransactionResponse> result = service.getAllTransactions();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Laptop", result.get(0).getDescription());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Printer", result.get(1).getDescription());
        verify(repository).findAll();
        verifyNoMoreInteractions(repository, treasuryApiClient, rateSelectionService);
    }

    @Test
    void getById_shouldReturnEntity_whenFound() {
        PurchaseTransaction entity = new PurchaseTransaction();
        entity.setId(10L);

        when(repository.findById(10L)).thenReturn(Optional.of(entity));

        PurchaseTransaction result = service.getById(10L);

        assertEquals(10L, result.getId());
    }

    @Test
    void getById_shouldThrowTransactionNotFoundException_whenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        TransactionNotFoundException ex =
                assertThrows(TransactionNotFoundException.class, () -> service.getById(99L));

        assertEquals("Transaction not found for id: 99", ex.getMessage());
    }
}