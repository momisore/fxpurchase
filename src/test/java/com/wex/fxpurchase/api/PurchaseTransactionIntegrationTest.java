package com.wex.fxpurchase.api;

import com.jayway.jsonpath.JsonPath;
import com.wex.fxpurchase.domain.ExchangeRate;
import com.wex.fxpurchase.infrastructure.treasury.TreasuryApiClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PurchaseTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreasuryApiClient treasuryApiClient;

    @Test
    void createThenConvert_shouldSucceed() throws Exception {
        when(treasuryApiClient.fetchRates("EUR")).thenReturn(List.of(
                new ExchangeRate("EUR", LocalDate.of(2026, 5, 20), new BigDecimal("0.92")),
                new ExchangeRate("EUR", LocalDate.of(2026, 4, 15), new BigDecimal("0.90"))
        ));

        String createPayload = """
                {
                  "description": "Office laptop",
                  "transactionDate": "2026-05-24",
                  "usdAmount": 1500.00
                }
                """;

        MvcResult created = mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String body = created.getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");
        long transactionId = id.longValue();

        mockMvc.perform(get("/api/transactions/{id}/converted", transactionId)
                        .param("targetCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.exchangeRate").value(0.92))
                .andExpect(jsonPath("$.convertedAmount").value(1380.00));
    }

    @Test
    void convert_shouldReturnNoEligibleRate_whenRateListHasNoEligibleEntries() throws Exception {
        when(treasuryApiClient.fetchRates("EUR")).thenReturn(List.of(
                new ExchangeRate("EUR", LocalDate.of(2026, 6, 1), new BigDecimal("0.95")),
                new ExchangeRate("EUR", LocalDate.of(2025, 1, 1), new BigDecimal("0.70"))
        ));

        String createPayload = """
                {
                  "description": "Printer",
                  "transactionDate": "2026-05-24",
                  "usdAmount": 500.00
                }
                """;

        MvcResult created = mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        long transactionId = id.longValue();

        mockMvc.perform(get("/api/transactions/{id}/converted", transactionId)
                        .param("targetCurrency", "EUR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_ELIGIBLE_RATE"))
                .andExpect(jsonPath("$.message")
                        .value("Purchase cannot be converted to the target currency because no eligible exchange rate was found within 6 months on or before the purchase date: EUR"));
    }

    @Test
    void convert_shouldReturnTransactionNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}/converted", 999999L)
                        .param("targetCurrency", "EUR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TRANSACTION_NOT_FOUND"));
    }
}