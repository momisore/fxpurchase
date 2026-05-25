package com.wex.fxpurchase.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API error payload used for validation and business errors.
 */
public class ApiErrorResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private List<String> details;

    public ApiErrorResponse() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}