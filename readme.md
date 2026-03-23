<p align="center">
  <img src="airtel-money.png" alt="Airtel Money" width="400" style="max-width: 100%;"/>
</p>

<h1 align="center">airtel-money-client</h1>

<p align="center">
  <strong>Auto-generated, strongly typed clients for the Airtel Money Collection API</strong><br/>
  Covers OAuth2 authentication, USSD Push payment initiation, and transaction status enquiry
</p>

<p align="center">
  <a href="https://github.com/Abega1642/airtel-money-client/actions/workflows/ci-test.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Abega1642/airtel-money-client/ci-test.yml?style=for-the-badge&label=tests&logo=githubactions&logoColor=white" />
  </a>
  <a href="https://github.com/Abega1642/airtel-money-client/actions/workflows/ci-publish-client.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Abega1642/airtel-money-client/ci-publish-client.yml?style=for-the-badge&label=java+publish&logo=githubactions&logoColor=white" />
  </a>
  <a href="https://github.com/Abega1642/airtel-money-client/actions/workflows/ci-publish-ts-client.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Abega1642/airtel-money-client/ci-publish-ts-client.yml?style=for-the-badge&label=ts+publish&logo=githubactions&logoColor=white" />
  </a>
  <a href="https://github.com/Abega1642/airtel-money-client/actions/workflows/ci-codeql.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Abega1642/airtel-money-client/ci-codeql.yml?style=for-the-badge&label=codeql&logo=githubactions&logoColor=white" />
  </a>
  <img src="https://img.shields.io/badge/version-1.0.0-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/OpenAPI-3.0.3-6BA539?style=for-the-badge&logo=openapiinitiative&logoColor=white" />
</p>

<p align="center">
  <img src="https://skillicons.dev/icons?i=java,spring,ts,npm&theme=light" />
</p>

<p align="center">
  <sub>Designed and maintained by <a href="https://github.com/Abega1642">Abegà Razafindratelo</a></sub>
</p>

---

## Table of Contents

- [Introduction](#introduction)
- [Why This Repository Exists](#why-this-repository-exists)
- [Repository Structure](#repository-structure)
- [API Overview](#api-overview)
    - [Base URLs](#base-urls)
    - [Authentication](#authentication)
    - [Payment Initiation](#payment-initiation)
    - [Transaction Status Enquiry](#transaction-status-enquiry)
    - [Transaction Status Codes](#transaction-status-codes)
    - [The Status Envelope](#the-status-envelope)
- [Java Client](#java-client)
    - [Requirements](#requirements)
    - [Installation](#installation)
    - [Configuration](#configuration)
    - [Usage](#usage)
- [TypeScript Client](#typescript-client)
    - [Requirements](#requirements-1)
    - [Installation](#installation-1)
    - [Configuration](#configuration-1)
    - [Usage](#usage-1)
- [Important Considerations](#important-considerations)
- [Support and Resources](#support-and-resources)

---

## Introduction

**airtel-money-client** provides auto-generated, strongly typed API clients for the **Airtel Money Collection API**, targeting the Madagascar market. Two client packages are published from this repository: a **Java client** distributed via GitHub Packages (Maven), and a **TypeScript client** distributed via GitHub Packages (npm).

Both clients are generated from a single OpenAPI 3.0.3 specification located at `doc/api.yaml` in this repository. Every time that specification changes, the clients are automatically regenerated, versioned, and published through GitHub Actions CI/CD pipelines. This ensures that the published packages always reflect the current state of the API contract.

The clients cover all three operations required for merchant payment collection using the Airtel Money platform:

- Acquiring an OAuth2 bearer token via the Client Credentials grant
- Initiating a USSD Push payment to a subscriber's Airtel Money wallet
- Querying the status of a previously initiated transaction

---

## Why This Repository Exists

Airtel Money does not publish an official SDK or typed client library for its Collection API. Developers integrating with the platform are expected to construct raw HTTP requests manually, which is error-prone, verbose, and difficult to maintain across teams.

This repository addresses that gap by generating strongly typed clients from the official OpenAPI specification. The result is a consistent, versioned, and maintainable integration layer that any Spring Boot application or TypeScript backend can depend on directly as a package, without having to deal with HTTP-level details.

---

## Repository Structure

The repository contains the OpenAPI specification, the Gradle build configuration that drives client generation, shell scripts that handle publishing to GitHub Packages, and CI/CD workflows that automate the full pipeline on every change to `doc/api.yaml`.

The generated client sources are not committed to the repository. They are produced at build time under `build/generated/` and published directly from there. This keeps the repository clean and ensures that published artifacts always correspond to the current specification.

---

## API Overview

The Airtel Money Collection API for Madagascar exposes three endpoints. All request and response payloads use `snake_case` field names. All requests must include appropriate headers as described in each section below.

### Base URLs

| Environment | Base URL |
|-------------|----------|
| Staging | `https://openapiuat.airtel.mg` |
| Production | `https://openapi.airtel.mg` |

The country code for Madagascar is `MG` and the currency code is `MGA`. These values must be provided in the `X-Country` and `X-Currency` headers on every request to the Collection and Transaction endpoints.

### Authentication

Before calling any Collection or Transaction endpoint, your application must obtain a bearer token from the authentication endpoint. This token is issued using the OAuth2 Client Credentials grant and must be included as a `Bearer` token in the `Authorization` header of all subsequent requests.

**Endpoint:** `POST /auth/oauth2/token`

**Request body:**

```json
{
  "client_id": "your-consumer-key",
  "client_secret": "your-consumer-secret",
  "grant_type": "client_credentials"
}
```

The `client_id` corresponds to the Consumer Key and `client_secret` corresponds to the Consumer Secret found in your application settings on the Airtel Money developer portal.

**Response:**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "expires_in": "3600",
  "token_type": "bearer"
}
```

Tokens are valid for 3600 seconds (60 minutes). Your application should cache the token and refresh it proactively before it expires to avoid unnecessary round trips and request failures during active sessions.

### Payment Initiation

Once a valid bearer token has been obtained, your application can initiate a USSD Push payment. This sends a notification to the subscriber's mobile device, prompting them to enter their Airtel Money PIN to authorise the transaction.

**Endpoint:** `POST /merchant/v1/payments/`

**Required headers:**

| Header | Value |
|--------|-------|
| `Authorization` | `Bearer <access_token>` |
| `X-Country` | `MG` |
| `X-Currency` | `MGA` |
| `Content-Type` | `application/json` |
| `Accept` | `*/*` |

**Request body:**

```json
{
  "reference": "order-ref-12345",
  "subscriber": {
    "country": "MG",
    "currency": "MGA",
    "msisdn": "330000003"
  },
  "transaction": {
    "amount": 1000,
    "country": "MG",
    "currency": "MGA",
    "id": "your-unique-transaction-id"
  }
}
```

Several fields in this request body require careful attention:

**`transaction.id`** must be a unique identifier generated by your own system. This is not an Airtel-generated ID. It is the key used to correlate this payment initiation with subsequent status enquiries. It must be unique per transaction.

**`subscriber.msisdn`** must be the subscriber's mobile number without the country code prefix. For Madagascar, strip the leading `261`. For example, send `330000003` and not `261330000003`.

**`reference`** serves as an idempotency key. If a network error occurs and you must retry the request, sending the same `reference` value prevents the customer from being charged twice for the same operation. The reference must not exceed 25 characters.

**Response:**

A `200 OK` response does not mean the payment has succeeded. It means only that Airtel has accepted the instruction and will push a USSD notification to the subscriber. The `data.transaction.status` field in the response reflects the immediate acceptance status, not the final payment outcome.

```json
{
  "data": {
    "transaction": {
      "id": "your-unique-transaction-id",
      "status": "SUCCESS"
    }
  },
  "status": {
    "code": "200",
    "message": "SUCCESS",
    "response_code": "DP00800001006",
    "result_code": "ESB000010",
    "success": true
  }
}
```

The final payment outcome must be determined by polling the transaction status endpoint or by receiving a callback if one has been configured in your application settings on the Airtel portal.

### Transaction Status Enquiry

After initiating a payment, use this endpoint to query its current status. It is strongly recommended to wait at least 3 minutes after initiating the payment before performing a status enquiry, to allow the subscriber sufficient time to respond to the USSD prompt.

**Endpoint:** `GET /standard/v1/payments/{id}`

The `{id}` path parameter is the `transaction.id` value that your system generated and sent in the payment initiation request. It is not an Airtel-generated ID.

**Required headers:**

| Header | Value |
|--------|-------|
| `Authorization` | `Bearer <access_token>` |
| `X-Country` | `MG` |
| `X-Currency` | `MGA` |
| `Accept` | `*/*` |

**Response:**

```json
{
  "data": {
    "transaction": {
      "id": "your-unique-transaction-id",
      "airtel_money_id": "C36xxxxxxx67",
      "status": "TS",
      "message": "success"
    }
  },
  "status": {
    "code": "200",
    "message": "SUCCESS",
    "response_code": "DP00800001001",
    "success": true
  }
}
```

The `data.transaction.airtel_money_id` field contains the transaction identifier assigned by Airtel's system. It is only present when the transaction has reached the terminal success state (`TS`).

### Transaction Status Codes

The `data.transaction.status` field in the enquiry response carries one of the following values:

| Code | Meaning |
|------|---------|
| `TS` | Transaction Success — the payment was completed successfully |
| `TF` | Transaction Failed — the payment was rejected or the subscriber declined |
| `TIP` | Transaction In Progress — the subscriber has not yet responded to the USSD prompt |
| `TA` | Transaction Ambiguous — the transaction is still being processed; perform another enquiry later |
| `TE` | Transaction Expired — the USSD session timed out before the subscriber responded |

### The Status Envelope

Every API response, regardless of whether the operation succeeded or failed, includes a `status` object at the top level. This envelope contains a `success` boolean field.

A critical point that is frequently misunderstood: `success: true` means only that the API processed the request without a system-level error. It does not mean the payment succeeded. In certain edge cases, the `success` field may be `false` even on an HTTP `200` response. The definitive source of truth for payment outcome is always `data.transaction.status`, not `status.success`.

---

## Java Client

### Requirements

- Java 21 or higher
- Spring Boot 3.2 or higher (the client uses `RestClient`, which requires Spring 6.1+)
- A GitHub account with a personal access token that has the `read:packages` scope

### Installation

The Java client is published to GitHub Packages under the Maven group `dev.razafindratelo` with the artifact ID `airtel-money-client-ts-gen`.

**Step 1.** Add your GitHub credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=your-github-username
gpr.key=your-github-personal-access-token
```

**Step 2.** Add the GitHub Packages repository to your `build.gradle`:

```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Abega1642/airtel-money-client")
        credentials {
            username = project.findProperty("gpr.user")
            password = project.findProperty("gpr.key")
        }
    }
    mavenCentral()
}
```

**Step 3.** Add the dependency:

```groovy
dependencies {
    implementation 'dev.razafindratelo:airtel-money-client-ts-gen:1.0.0'
}
```

For Gradle Kotlin DSL:

```kotlin
dependencies {
    implementation("dev.razafindratelo:airtel-money-client-ts-gen:1.0.0")
}
```

For Maven:

```xml
<dependency>
    <groupId>dev.razafindratelo</groupId>
    <artifactId>airtel-money-client-ts-gen</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration

The client is built on top of Spring's `RestClient`. You configure the base URL through the `ApiClient` instance before constructing any API object:

```java
import dev.razafindratelo.airtel_money_client.invoker.ApiClient;
import dev.razafindratelo.airtel_money_client.api.AuthenticationApi;
import dev.razafindratelo.airtel_money_client.api.CollectionApi;
import dev.razafindratelo.airtel_money_client.api.TransactionApi;

ApiClient apiClient = new ApiClient();
apiClient.setBasePath("https://openapiuat.airtel.mg"); // use staging
// apiClient.setBasePath("https://openapi.airtel.mg"); // use production

AuthenticationApi authApi = new AuthenticationApi(apiClient);
CollectionApi collectionApi = new CollectionApi(apiClient);
TransactionApi transactionApi = new TransactionApi(apiClient);
```

In a Spring Boot application, you would typically register these as beans in a configuration class and inject them where needed.

### Usage

**Step 1. Acquire a bearer token**

```java
import dev.razafindratelo.airtel_money_client.model.TokenRequest;
import dev.razafindratelo.airtel_money_client.model.TokenResponse;

var tokenRequest = new TokenRequest();
tokenRequest.setClientId("your-consumer-key");
tokenRequest.setClientSecret("your-consumer-secret");
tokenRequest.setGrantType(TokenRequest.GrantTypeEnum.CLIENT_CREDENTIALS);

TokenResponse tokenResponse = authApi.getAccessToken(
        "application/json",
        "application/json",
        tokenRequest
);

String bearerToken = "Bearer " + tokenResponse.getAccessToken();
```

Cache this token and reuse it across requests until it is close to expiry. The `expires_in` field in the response gives the validity duration in seconds.

**Step 2. Initiate a payment**

```java
import dev.razafindratelo.airtel_money_client.model.*;
import java.math.BigDecimal;
import java.util.UUID;

var subscriber = new PaymentSubscriber();
subscriber.setMsisdn("330000003"); // local number only, no country code prefix
subscriber.setCountry("MG");
subscriber.setCurrency("MGA");

var transaction = new PaymentTransaction();
transaction.setId(UUID.randomUUID().toString()); // your system's unique transaction ID
transaction.setAmount(BigDecimal.valueOf(1000));
transaction.setCountry("MG");
transaction.setCurrency("MGA");

var paymentRequest = new PaymentRequest();
paymentRequest.setReference("order-ref-12345"); // idempotency key, max 25 characters
paymentRequest.setSubscriber(subscriber);
paymentRequest.setTransaction(transaction);

PaymentInitiationResponse response = collectionApi.initiatePayment(
        "*/*",                // Accept
        "application/json",   // Content-Type
        "MG",                 // X-Country
        "MGA",                // X-Currency
        bearerToken,          // Authorization
        paymentRequest
);
```

**Step 3. Query the transaction status**

Wait at least 3 minutes after initiating the payment before querying, to allow the subscriber time to respond to the USSD prompt.

```java
import dev.razafindratelo.airtel_money_client.model.TransactionEnquiryResponse;
import dev.razafindratelo.airtel_money_client.model.TransactionStatus;

TransactionEnquiryResponse enquiry = transactionApi.getTransactionStatus(
        transactionId,  // the same transaction.id you sent in the initiation request
        "*/*",          // Accept
        "MG",           // X-Country
        "MGA",          // X-Currency
        bearerToken     // Authorization
);

TransactionStatus status = enquiry.getData().getTransaction().getStatus();

if (status == TransactionStatus.TS) {
    String airtelMoneyId = enquiry.getData().getTransaction().getAirtelMoneyId();
    // payment confirmed successful
} else if (status == TransactionStatus.TIP || status == TransactionStatus.TA) {
    // still in progress, retry the enquiry later
} else {
    // TF or TE — payment failed or expired
}
```

Each API method is also available in two additional variants. `getTransactionStatusWithHttpInfo` returns a `ResponseEntity` that gives you access to the HTTP status code alongside the response body. `getTransactionStatusWithResponseSpec` returns the raw Spring `ResponseSpec` for cases where you want to handle the response yourself. The same applies to `getAccessToken` and `initiatePayment`.

---

## TypeScript Client

### Requirements

- Node.js 18 or higher
- npm 9 or higher
- A GitHub account with a personal access token that has the `read:packages` scope

### Installation

The TypeScript client is published to GitHub Packages under the npm scope `@Abega1642` with the package name `airtel-money-client-ts-gen`. The client is generated using the `typescript-fetch` generator, meaning it uses the native `fetch` API and has no external runtime dependencies.

**Step 1.** Create or update the `.npmrc` file in your project root to point to GitHub Packages for the scoped package:

```
@Abega1642:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=your-github-personal-access-token
```

**_Do not commit this file to source control if it contains your token. Use environment variables or a secrets manager instead._**

**Step 2.** Install the package:

```bash
npm install @Abega1642/airtel-money-client-ts-gen
```

### Configuration

The TypeScript client exposes a `Configuration` class that accepts the base URL and other options. Pass a configured instance to each API class at construction time:

```typescript
import { Configuration, AuthenticationApi, CollectionApi, TransactionApi } from '@Abega1642/airtel-money-client-ts-gen';

const config = new Configuration({
    basePath: 'https://openapiuat.airtel.mg', // staging
    // basePath: 'https://openapi.airtel.mg', // production
});

const authApi = new AuthenticationApi(config);
const collectionApi = new CollectionApi(config);
const transactionApi = new TransactionApi(config);
```

### Usage

**Step 1. Acquire a bearer token**

```typescript
import { TokenRequest } from '@Abega1642/airtel-money-client-ts-gen';

const tokenResponse = await authApi.getAccessToken({
    contentType: 'application/json',
    accept: 'application/json',
    tokenRequest: {
        client_id: 'your-consumer-key',
        client_secret: 'your-consumer-secret',
        grant_type: 'client_credentials',
    } as TokenRequest,
});

const bearerToken = `Bearer ${tokenResponse.access_token}`;
```

Cache and reuse this token across requests. The `expires_in` field gives the validity duration in seconds as a string.

**Step 2. Initiate a payment**

```typescript
import { PaymentRequest } from '@Abega1642/airtel-money-client-ts-gen';
import { randomUUID } from 'crypto';

const transactionId = randomUUID();

const initiationResponse = await collectionApi.initiatePayment({
    accept: '*/*',
    contentType: 'application/json',
    xCountry: 'MG',
    xCurrency: 'MGA',
    authorization: bearerToken,
    paymentRequest: {
        reference: 'order-ref-12345',
        subscriber: {
            msisdn: '330000003', // local number only, no country code prefix
            country: 'MG',
            currency: 'MGA',
        },
        transaction: {
            id: transactionId,
            amount: 1000,
            country: 'MG',
            currency: 'MGA',
        },
    } as PaymentRequest,
});
```

**Step 3. Query the transaction status**

```typescript
import { TransactionStatus } from '@Abega1642/airtel-money-client-ts-gen';

const enquiry = await transactionApi.getTransactionStatus({
    id: transactionId,
    accept: '*/*',
    xCountry: 'MG',
    xCurrency: 'MGA',
    authorization: bearerToken,
});

const status = enquiry.data?.transaction?.status;

if (status === TransactionStatus.Ts) {
    const airtelMoneyId = enquiry.data?.transaction?.airtel_money_id;
    // payment confirmed successful
} else if (status === TransactionStatus.Tip || status === TransactionStatus.Ta) {
    // still in progress, retry the enquiry later
} else {
    // TF or TE — payment failed or expired
}
```

---

## Important Considerations

**Token caching.** Acquiring a new token on every request is unnecessary and inefficient. Tokens are valid for 3600 seconds. Your application should cache the token and refresh it only when it is close to expiry.

**HTTP 200 does not mean payment success.** A `200 OK` from the payment initiation endpoint confirms only that Airtel has queued the USSD push to the subscriber. The subscriber may still decline, fail to respond, or have insufficient funds. Always determine the final outcome through the transaction status enquiry.

**`status.success` is not the payment outcome.** The `success` boolean in the status envelope reflects whether the API call was processed without a system error. It is not a payment confirmation. Always use `data.transaction.status` for the definitive outcome.

**The `msisdn` field must exclude the country code.** For Madagascar, strip the leading `261` from the subscriber's number. Send `330000003`, not `261330000003`.

**Idempotency via `reference`.** If a payment initiation request fails due to a network error and you need to retry, use the same `reference` value. Airtel uses this field to detect duplicate requests and will not charge the subscriber twice for the same reference.

**Transaction enquiry timing.** Wait at least 3 minutes after initiating a payment before querying its status. Querying immediately after initiation will typically return `TIP` (Transaction In Progress) because the subscriber has not yet had time to respond. For `TA` (Transaction Ambiguous) responses, retry the enquiry after a short delay.

**Error handling on timeout responses (408, 502, 504).** For these error codes on payment or refund operations, do not retry immediately. Perform a transaction status enquiry first to determine whether the payment was actually processed before sending a new initiation request. Retrying without checking could result in duplicate charges.

---

## Support and Resources

- **Repository:** [https://github.com/Abega1642/airtel-money-client](https://github.com/Abega1642/airtel-money-client)
- **Issue Tracker:** [https://github.com/Abega1642/airtel-money-client/issues](https://github.com/Abega1642/airtel-money-client/issues)
- **Airtel Money Developer Portal:** [https://developers.airtel.africa](https://developers.airtel.africa)
- **OpenAPI Specification:** [`doc/api.yaml`](doc/api.yaml)
- **Author:** Abegà Razafindratelo

---

<p align="center">
  <sub>This repository is not affiliated with or endorsed by Airtel Africa. It is an independent, community-maintained client library built to facilitate integration with the Airtel Money Collection API.</sub>
</p>