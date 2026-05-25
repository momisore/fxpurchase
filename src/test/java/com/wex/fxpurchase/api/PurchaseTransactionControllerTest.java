package com.wex.fxpurchase.api;

import com.wex.fxpurchase.api.dto.ConvertedTransactionResponse;
import com.wex.fxpurchase.api.dto.CreatePurchaseTransactionResponse;
import com.wex.fxpurchase.application.PurchaseTransactionService;
import com.wex.fxpurchase.application.exception.NoEligibleRateFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseTransactionController.class)
@Import(ApiExceptionHandler.class)
class PurchaseTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseTransactionService service;

    @Test
    void createTransaction_shouldReturn201_whenRequestIsValid() throws Exception {
        CreatePurchaseTransactionResponse response = new CreatePurchaseTransactionResponse();
        response.setId(100L);
        response.setDescription("Laptop");
        response.setTransactionDate(LocalDate.of(2026, 5, 24));
        response.setUsdAmount(new BigDecimal("1500.00"));
        response.setCreatedAt(LocalDateTime.of(2026, 5, 24, 10, 0, 0));
        response.setUpdatedAt(LocalDateTime.of(2026, 5, 24, 10, 0, 0));

        when(service.create(any())).thenReturn(response);

        String payload = """
                {
                  "description": "Laptop",
                  "transactionDate": "2026-05-24",
                  "usdAmount": 1500.00
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/transactions/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.description").value("Laptop"))
                .andExpect(jsonPath("$.transactionDate").value("2026-05-24"))
                .andExpect(jsonPath("$.usdAmount").value(1500.00));
    }

    @Test
    void createTransaction_shouldReturn400_whenValidationFails() throws Exception {
        String payload = """
                {
                  "description": "",
                  "transactionDate": "2026-05-24",
                  "usdAmount": -2
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

        @Test
        void getAllTransactions_shouldReturnAllItemsWithIds() throws Exception {
                CreatePurchaseTransactionResponse first = new CreatePurchaseTransactionResponse();
                first.setId(1L);
                first.setDescription("Laptop");
                first.setTransactionDate(LocalDate.of(2026, 5, 24));
                first.setUsdAmount(new BigDecimal("1500.00"));

                CreatePurchaseTransactionResponse second = new CreatePurchaseTransactionResponse();
                second.setId(2L);
                second.setDescription("Printer");
                second.setTransactionDate(LocalDate.of(2026, 5, 23));
                second.setUsdAmount(new BigDecimal("500.00"));

                when(service.getAllTransactions()).thenReturn(List.of(first, second));

                mockMvc.perform(get("/api/transactions"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].description").value("Laptop"))
                                .andExpect(jsonPath("$[1].id").value(2))
                                .andExpect(jsonPath("$[1].description").value("Printer"));
        }

    @Test
    void getConvertedTransaction_shouldReturn200_whenEligibleRateExists() throws Exception {
        ConvertedTransactionResponse response = new ConvertedTransactionResponse();
        response.setId(100L);
        response.setDescription("Laptop");
        response.setTransactionDate(LocalDate.of(2026, 5, 24));
        response.setUsdAmount(new BigDecimal("1500.00"));
        response.setExchangeRate(new BigDecimal("0.92"));
        response.setConvertedAmount(new BigDecimal("1380.00"));
        response.setTargetCurrency("EUR");
        response.setRateDateUsed(LocalDate.of(2026, 5, 20));

        when(service.getConvertedTransaction(100L, "EUR")).thenReturn(response);

        mockMvc.perform(get("/api/transactions/100/converted")
                        .param("targetCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.description").value("Laptop"))
                .andExpect(jsonPath("$.transactionDate").value("2026-05-24"))
                .andExpect(jsonPath("$.usdAmount").value(1500.00))
                .andExpect(jsonPath("$.exchangeRate").value(0.92))
                .andExpect(jsonPath("$.convertedAmount").value(1380.00))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.rateDateUsed").value("2026-05-20"));
    }

    @Test
    void getConvertedTransaction_shouldReturn404_whenNoEligibleRateExists() throws Exception {
        when(service.getConvertedTransaction(100L, "EUR"))
                .thenThrow(new NoEligibleRateFoundException("EUR"));

        mockMvc.perform(get("/api/transactions/100/converted")
                        .param("targetCurrency", "EUR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_ELIGIBLE_RATE"))
                .andExpect(jsonPath("$.message")
                        .value("Purchase cannot be converted to the target currency because no eligible exchange rate was found within 6 months on or before the purchase date: EUR"));
    }
}