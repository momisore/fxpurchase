package com.wex.fxpurchase.api;

import com.wex.fxpurchase.api.dto.ApiErrorResponse;
import com.wex.fxpurchase.application.exception.NoEligibleRateFoundException;
import com.wex.fxpurchase.application.exception.TransactionNotFoundException;
import com.wex.fxpurchase.infrastructure.treasury.TreasuryApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        ApiErrorResponse error = new ApiErrorResponse();
        error.setCode("VALIDATION_ERROR");
        error.setMessage("Request validation failed");
        error.setTimestamp(LocalDateTime.now());
        error.setPath(request.getRequestURI());
        error.setDetails(details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionNotFound(
            TransactionNotFoundException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse();
        error.setCode("TRANSACTION_NOT_FOUND");
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setPath(request.getRequestURI());
        error.setDetails(List.of());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoEligibleRateFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoEligibleRate(
            NoEligibleRateFoundException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse();
        error.setCode("NO_ELIGIBLE_RATE");
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setPath(request.getRequestURI());
        error.setDetails(List.of());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TreasuryApiException.class)
    public ResponseEntity<ApiErrorResponse> handleTreasuryApiFailure(
            TreasuryApiException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse();
        error.setCode("TREASURY_API_ERROR");
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setPath(request.getRequestURI());
        error.setDetails(List.of());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }
}