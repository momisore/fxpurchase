# WEX FX Purchase App

## Overview
This is a Spring Boot service for managing FX purchase transactions.


Current capabilities:
1. Create USD purchase transactions with validation and standardized error responses.
2. Retrieve converted transaction values by target currency.
3. Apply the 6-month Treasury rate eligibility rule.
4. Return consistent business and validation errors.
5. Include request correlation id in response headers and log context.

## Submission Traceability: Product Brief Criteria

This submission is intentionally structured to demonstrate product thinking, collaboration, disciplined execution, and complex problem-solving in a Corporate Payments FX workflow. The sections below map each criterion to specific implementation evidence.

### 1. Product Thinking and Strategic Prioritization (Corporate Payments)

What this demonstrates:
1. Prioritization of financial correctness and compliance-oriented business rules.
2. Design choices that protect transaction integrity and operational trust.

Evidence:
1. Business rule for rate eligibility window: src/main/java/com/wex/fxpurchase/application/RateSelectionService.java
2. Financial rounding and conversion precision controls: src/main/java/com/wex/fxpurchase/application/PurchaseTransactionService.java
3. Domain lifecycle and auditable timestamps: src/main/java/com/wex/fxpurchase/domain/PurchaseTransaction.java
4. Database-level transaction guardrails: src/main/resources/db/migration/V1__create_purchase_transactions.sql

### 2. Cross-Functional Collaboration Across Product, Engineering, and Stakeholders

What this demonstrates:
1. Clear API contracts for consumer teams.
2. Explicit boundaries between business logic, external integration, and operations.
3. Operability patterns that support support and operations workflows.

Evidence:
1. Input/output API contracts and validation: src/main/java/com/wex/fxpurchase/api/dto
2. Standardized error model for predictable downstream behavior: src/main/java/com/wex/fxpurchase/api/ApiExceptionHandler.java
3. Configuration ownership and environment tuning controls: src/main/java/com/wex/fxpurchase/config/TreasuryApiProperties.java
4. Request traceability across systems (correlation id): src/main/java/com/wex/fxpurchase/api/CorrelationIdFilter.java
5. Runtime observability configuration: src/main/resources/application.properties

### 3. Execution Plan: Delivery Phases, Risk Management, and Measurable Outcomes

What this demonstrates:
1. Incremental delivery through layered architecture.
2. Risk reduction through testing, resilience, and schema governance.
3. Outcome orientation through reliability and monitoring hooks.

Evidence:
1. Layered architecture entry point: src/main/java/com/wex/fxpurchase/FxpurchaseApplication.java
2. Core unit and service tests: src/test/java/com/wex/fxpurchase
3. API behavior and validation tests: src/test/java/com/wex/fxpurchase/api
4. Application logic tests: src/test/java/com/wex/fxpurchase/application
5. External dependency resilience approach (retry/backoff): src/main/java/com/wex/fxpurchase/infrastructure/treasury/TreasuryApiClient.java

Proposed measurable outcomes for review:
1. Conversion success rate.
2. Upstream Treasury dependency error rate.
3. End-to-end conversion latency.
4. Rate-eligibility failure rate.

### 4. Problem-Solving for Complex Technical and Organizational Challenges

What this demonstrates:
1. Handling real-world external data variability and integration fragility.
2. Constraining failures to the right boundary with explicit exception semantics.
3. Making business-critical behavior deterministic under edge cases.

Evidence:
1. Currency normalization and external response handling: src/main/java/com/wex/fxpurchase/infrastructure/treasury/TreasuryApiClient.java
2. Eligibility logic under temporal constraints: src/main/java/com/wex/fxpurchase/application/RateSelectionService.java
3. Upstream failure classification and API-safe mapping: src/main/java/com/wex/fxpurchase/api/ApiExceptionHandler.java
4. Transaction workflow orchestration: src/main/java/com/wex/fxpurchase/application/PurchaseTransactionService.java


## Tech Stack
1. Java 21
2. Spring Boot 3.5.14
3. Spring Web, Validation, Data JPA, Actuator
4. Flyway for schema migration
5. H2 (local/test), PostgreSQL (prod)
6. springdoc OpenAPI UI
7. JUnit 5 + Mockito + MockMvc

## Project Layout
1. src/main/java/com/wex/fxpurchase/api
2. src/main/java/com/wex/fxpurchase/application
3. src/main/java/com/wex/fxpurchase/domain
4. src/main/java/com/wex/fxpurchase/infrastructure
5. src/main/resources/db/migration
6. src/test/java/com/wex/fxpurchase

## Prerequisites
1. Java 21+ installed and available in PATH or JAVA_HOME.
2. No manual Maven install needed because this project uses the Maven wrapper.

## Configuration and Profiles
Shared properties are in src/main/resources/application.properties.

Profiles:
1. local: src/main/resources/application-local.properties
2. test: src/main/resources/application-test.properties
3. prod: src/main/resources/application-prod.properties

### Core Environment Variables
Optional for local/test (defaults exist), required for prod:
1. DB_URL
2. DB_USERNAME
3. DB_PASSWORD
4. DB_DRIVER (optional, defaults to org.postgresql.Driver)
5. TREASURY_API_BASE_URL
6. TREASURY_API_CONNECT_TIMEOUT_MS
7. TREASURY_API_READ_TIMEOUT_MS
8. TREASURY_API_MAX_RETRIES
9. TREASURY_API_RETRY_BACKOFF_MS

## Run the Application
From project root:

1. Run using default profile (local)
```bash
./mvnw spring-boot:run
```

2. Run explicitly with local profile
```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

3. Run explicitly with prod profile
```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://localhost:5432/fxpurchase \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

## Database Migration
Flyway migrations are in src/main/resources/db/migration.

Current migration:
1. V1__create_purchase_transactions.sql

Migrations execute on startup before JPA validation.

## API Endpoints

### Endpoint Summary
1. `POST /api/transactions` - create a purchase transaction.
2. `GET /api/transactions` - list all purchase transactions.
3. `GET /api/transactions/{id}/converted?targetCurrency=EUR` - retrieve the converted amount for one transaction.
4. `GET /actuator/health` - application health.
5. `GET /actuator/info` - application info.
6. `GET /v3/api-docs` - OpenAPI JSON.
7. `GET /swagger-ui/index.html` - Swagger UI.

### 1) Create Transaction
POST /api/transactions

Request headers:
1. `Content-Type: application/json`

Sample request:
```json
{
	"description": "Office laptop",
	"transactionDate": "2026-05-24",
	"usdAmount": 1500.00
}
```

Sample success response (201):
```json
{
	"id": 1,
	"description": "Office laptop",
	"transactionDate": "2026-05-24",
	"usdAmount": 1500.00,
	"createdAt": "2026-05-24T10:00:00",
	"updatedAt": "2026-05-24T10:00:00"
}
```

Expected error responses:
1. `400 VALIDATION_ERROR` if description is blank, transactionDate is missing, or usdAmount is not positive.
2. `500` only for unexpected server failures not handled by the API advice.

### 2) Get All Transactions
GET /api/transactions

Sample success response (200):
```json
[
	{
		"id": 1,
		"description": "Office laptop",
		"transactionDate": "2026-05-24",
		"usdAmount": 1500.00,
		"createdAt": "2026-05-24T10:00:00",
		"updatedAt": "2026-05-24T10:00:00"
	},
	{
		"id": 2,
		"description": "Printer",
		"transactionDate": "2026-05-23",
		"usdAmount": 500.00,
		"createdAt": "2026-05-24T10:05:00",
		"updatedAt": "2026-05-24T10:05:00"
	}
]
```

Expected responses:
1. `200 OK` with a JSON array of all transactions.
2. Empty array when no records exist.

### 3) Get Converted Transaction
GET /api/transactions/{id}/converted?targetCurrency=EUR

Request headers:
1. No special headers required.

Accepted `targetCurrency` formats:
1. ISO 4217 3-letter code, for example `EUR`, `NGN`, `BRL`.
2. Currency name, for example `Euro`, `Naira`, `Real`.
3. Treasury country-currency description, for example `Nigeria-Naira`, `Brazil-Real`, `Afghanistan-Afghani`.

Sample success response (200):
```json
{
	"id": 1,
	"description": "Office laptop",
	"transactionDate": "2026-05-24",
	"usdAmount": 1500.00,
	"exchangeRate": 0.92,
	"convertedAmount": 1380.00,
	"targetCurrency": "EUR",
	"rateDateUsed": "2026-05-20"
}
```

Expected error responses:
1. `404 TRANSACTION_NOT_FOUND` if the transaction id does not exist.
2. `404 NO_ELIGIBLE_RATE` if no Treasury rate is found on or before the purchase date within the previous 6 months.
3. `502 TREASURY_API_ERROR` if the Treasury API call fails or cannot be parsed.

### 4) Health and Info Endpoints
GET /actuator/health

Expected response:
1. `200 OK` with health status payload.

GET /actuator/info

Expected response:
1. `200 OK` with application info payload.

### 5) OpenAPI / Swagger Endpoints
GET /v3/api-docs

Expected response:
1. `200 OK` with OpenAPI JSON document.

GET /swagger-ui/index.html

Expected response:
1. `200 OK` with interactive Swagger UI.

## Error Model
All handled errors return this shape:
```json
{
	"code": "ERROR_CODE",
	"message": "Human readable message",
	"timestamp": "2026-05-24T18:30:00",
	"path": "/api/transactions/1/converted",
	"details": []
}
```

Error codes currently used:
1. VALIDATION_ERROR (HTTP 400)
2. TRANSACTION_NOT_FOUND (HTTP 404)
3. NO_ELIGIBLE_RATE (HTTP 404)
4. TREASURY_API_ERROR (HTTP 502)

### Common Error Examples

Validation error:
```json
{
	"code": "VALIDATION_ERROR",
	"message": "Request validation failed",
	"timestamp": "2026-05-24T18:30:00",
	"path": "/api/transactions",
	"details": [
		"description: description is required",
		"usdAmount: usdAmount must be positive"
	]
}
```

Transaction not found:
```json
{
	"code": "TRANSACTION_NOT_FOUND",
	"message": "Transaction not found for id: 99",
	"timestamp": "2026-05-24T18:30:00",
	"path": "/api/transactions/99/converted",
	"details": []
}
```

No eligible rate found:
```json
{
	"code": "NO_ELIGIBLE_RATE",
	"message": "Purchase cannot be converted to the target currency because no eligible exchange rate was found within 6 months on or before the purchase date: EUR",
	"timestamp": "2026-05-24T18:30:00",
	"path": "/api/transactions/99/converted",
	"details": []
}
```

Treasury API error:
```json
{
	"code": "TREASURY_API_ERROR",
	"message": "Failed to retrieve Treasury rates",
	"timestamp": "2026-05-24T18:30:00",
	"path": "/api/transactions/99/converted",
	"details": []
}
```

## Correlation Id
Every request gets an X-Correlation-Id header.

Behavior:
1. If client sends X-Correlation-Id, it is reused.
2. If missing, server generates one.
3. Value is echoed in response header.
4. Value is also added to MDC for logging.

## OpenAPI / Swagger
After app startup:
1. Swagger UI: /swagger-ui/index.html
2. OpenAPI JSON: /v3/api-docs

## Health and Info
Actuator endpoints exposed:
1. /actuator/health
2. /actuator/info

## Test Suite
Run all tests:
```bash
./mvnw clean test
```

Run one test class:
```bash
./mvnw -Dtest=PurchaseTransactionControllerTest test
```

Run one test method:
```bash
./mvnw -Dtest=PurchaseTransactionControllerTest#getConvertedTransaction_shouldReturn200_whenEligibleRateExists test
```

Current test classes:
1. src/test/java/com/wex/fxpurchase/FxpurchaseApplicationTests.java
2. src/test/java/com/wex/fxpurchase/PurchaseTransactionServiceTest.java
3. src/test/java/com/wex/fxpurchase/ApiExceptionHandlerTest.java
4. src/test/java/com/wex/fxpurchase/application/RateSelectionServiceTest.java
5. src/test/java/com/wex/fxpurchase/api/PurchaseTransactionControllerTest.java
6. src/test/java/com/wex/fxpurchase/api/PurchaseTransactionIntegrationTest.java
7. src/test/java/com/wex/fxpurchase/infrastructure/treasury/TreasuryApiClientTest.java

## Quick Curl Examples
Create:
```bash
curl -i -X POST http://localhost:8080/api/transactions \
	-H "Content-Type: application/json" \
	-d '{"description":"Office laptop","transactionDate":"2026-05-24","usdAmount":1500.00}'
```

Convert:
```bash
curl -i "http://localhost:8080/api/transactions/1/converted?targetCurrency=EUR"
```

## Troubleshooting
1. Error: missing table purchase_transactions
Cause: migration not applied or wrong profile.
Fix: run with local/test profile and confirm Flyway migration exists.

2. Error: transaction not found
Cause: requested id does not exist.
Fix: create a transaction first, then call converted endpoint.

3. Error: NO_ELIGIBLE_RATE
Cause: no Treasury rate on/before purchase date within 6 months.
Fix: verify rate dates and target currency input format (ISO code, currency name, or Treasury country-currency description).

4. Error: TREASURY_API_ERROR
Cause: external API issue, response parse issue, or connectivity issue.
Fix: verify treasury base URL and network access.

## Requirement Traceability
1. Create USD purchase transaction
Implementation:
src/main/java/com/wex/fxpurchase/api/PurchaseTransactionController.java
src/main/java/com/wex/fxpurchase/application/PurchaseTransactionService.java
Tests:
src/test/java/com/wex/fxpurchase/api/PurchaseTransactionControllerTest.java
src/test/java/com/wex/fxpurchase/PurchaseTransactionServiceTest.java

2. Validations enforced
Implementation:
src/main/java/com/wex/fxpurchase/api/dto/CreatePurchaseTransactionRequest.java
src/main/java/com/wex/fxpurchase/api/ApiExceptionHandler.java
Tests:
src/test/java/com/wex/fxpurchase/api/PurchaseTransactionControllerTest.java

3. Retrieve converted transaction with 6-month rule
Implementation:
src/main/java/com/wex/fxpurchase/api/PurchaseTransactionController.java
src/main/java/com/wex/fxpurchase/application/PurchaseTransactionService.java
src/main/java/com/wex/fxpurchase/application/RateSelectionService.java
Tests:
src/test/java/com/wex/fxpurchase/application/RateSelectionServiceTest.java
src/test/java/com/wex/fxpurchase/api/PurchaseTransactionControllerTest.java

4. Correct error when no qualifying rate exists
Implementation:
src/main/java/com/wex/fxpurchase/application/exception/NoEligibleRateFoundException.java
src/main/java/com/wex/fxpurchase/api/ApiExceptionHandler.java
Tests:
src/test/java/com/wex/fxpurchase/api/PurchaseTransactionControllerTest.java

## Assumptions and Tradeoffs
1. Local/test use H2 in-memory database for quick iteration.
2. Treasury response mapping assumes current fiscaldata field names.
3. Retry strategy is simple and property-driven.
4. Error payload format is standardized for handled exceptions.

## Fresh-Machine Reproducibility
1. Clone repository.
2. Ensure Java 21+.
3. Run ./mvnw clean test.
4. Run ./mvnw spring-boot:run.
5. Execute sample API requests above.
