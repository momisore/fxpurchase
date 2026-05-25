package com.wex.fxpurchase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Binds Treasury API settings from application properties under the treasury.api prefix.
 * Centralizes external API configuration such as base URL, timeouts, and retry behavior.
 */

@ConfigurationProperties(prefix = "treasury.api")
public class TreasuryApiProperties {

    // Base URL for the Treasury API
    private String baseUrl;

    // HTTP connection timeout in milliseconds
    private int connectTimeoutMs;

    // HTTP read timeout in milliseconds
    private int readTimeoutMs;

    // Maximum number of retries for failed requests
    private int maxRetries;

    // Backoff time in milliseconds between retries
    private int retryBackoffMs;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(int retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

}
