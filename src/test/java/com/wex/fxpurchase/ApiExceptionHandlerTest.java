package com.wex.fxpurchase;

import com.wex.fxpurchase.api.ApiExceptionHandler;
import com.wex.fxpurchase.api.dto.ApiErrorResponse;
import com.wex.fxpurchase.application.exception.TransactionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    @Test
    void handleTransactionNotFound_shouldReturn404AndStandardPayload() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/transactions/99");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleTransactionNotFound(new TransactionNotFoundException(99L), request);

        assertEquals(404, response.getStatusCode().value());

        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("TRANSACTION_NOT_FOUND", body.getCode());
        assertEquals("Transaction not found for id: 99", body.getMessage());
        assertEquals("/api/transactions/99", body.getPath());
    }
}