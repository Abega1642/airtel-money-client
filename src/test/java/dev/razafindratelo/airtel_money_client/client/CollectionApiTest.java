package dev.razafindratelo.airtel_money_client.client;

import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.ACCEPT;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.CONTENT_TYPE;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.MSISDN_FAILURE;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.MSISDN_PENDING;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.MSISDN_SUCCESS;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.X_COUNTRY;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.X_CURRENCY;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aBearerToken;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aFailureStatusEnvelope;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aPaymentInitiationResponse;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aPaymentRequest;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.razafindratelo.airtel_money_client.api.CollectionApi;
import dev.razafindratelo.airtel_money_client.invoker.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class CollectionApiTest {

  @Mock private ApiClient apiClient;
  @Mock private RestClient.ResponseSpec responseSpec;

  private CollectionApi collectionApi;

  @BeforeEach
  void setUp() {
    collectionApi = new CollectionApi(apiClient);
  }

  private void stubInvokeAPI() {
    doReturn(responseSpec)
        .when(apiClient)
        .invokeAPI(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void initiate_payment_success_scenario_returns_accepted_response_with_transaction_id() {
    var txId = randomUUID().toString();
    var expected = aPaymentInitiationResponse(txId, "DP");

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result =
        collectionApi.initiatePayment(
            ACCEPT,
            CONTENT_TYPE,
            X_COUNTRY,
            X_CURRENCY,
            aBearerToken(),
            aPaymentRequest(MSISDN_SUCCESS));

    assertNotNull(result);
    assertNotNull(result.getData());
    assertNotNull(result.getStatus());
    assertNotNull(result.getData().getTransaction());
    assertEquals(txId, result.getData().getTransaction().getId());
    assertTrue(result.getStatus().getSuccess());
    assertEquals("200", result.getStatus().getCode());
  }

  @Test
  void initiate_payment_failure_scenario_returns_response_with_failure_status() {
    var txId = randomUUID().toString();
    var response = aPaymentInitiationResponse(txId, "TF");
    response.setStatus(aFailureStatusEnvelope("200", "Transaction Failed"));

    stubInvokeAPI();
    doReturn(response).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result =
        collectionApi.initiatePayment(
            ACCEPT,
            CONTENT_TYPE,
            X_COUNTRY,
            X_CURRENCY,
            aBearerToken(),
            aPaymentRequest(MSISDN_FAILURE));

    assertNotNull(result);
    assertFalse(result.getStatus().getSuccess());
    assertNotNull(result.getData().getTransaction());
    assertEquals("TF", result.getData().getTransaction().getStatus());
  }

  @Test
  void initiate_payment_pending_scenario_returns_response_with_in_progress_status() {
    var txId = randomUUID().toString();
    var response = aPaymentInitiationResponse(txId, "TIP");

    stubInvokeAPI();
    doReturn(response).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result =
        collectionApi.initiatePayment(
            ACCEPT,
            CONTENT_TYPE,
            X_COUNTRY,
            X_CURRENCY,
            aBearerToken(),
            aPaymentRequest(MSISDN_PENDING));

    assertNotNull(result);
    assertEquals("TIP", result.getData().getTransaction().getStatus());
  }

  @Test
  void initiate_payment_uses_correct_endpoint_and_http_method() {
    stubInvokeAPI();
    doReturn(aPaymentInitiationResponse(randomUUID().toString(), "DP"))
        .when(responseSpec)
        .body(any(ParameterizedTypeReference.class));

    collectionApi.initiatePayment(
        ACCEPT,
        CONTENT_TYPE,
        X_COUNTRY,
        X_CURRENCY,
        aBearerToken(),
        aPaymentRequest(MSISDN_SUCCESS));

    verify(apiClient)
        .invokeAPI(
            eq("/merchant/v1/payments/"),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any());
  }

  @Test
  void initiate_payment_with_http_info_returns_response_entity_with_200_and_body() {
    var txId = randomUUID().toString();
    var entity = ResponseEntity.ok(aPaymentInitiationResponse(txId, "DP"));

    stubInvokeAPI();
    doReturn(entity).when(responseSpec).toEntity(any(ParameterizedTypeReference.class));

    var result =
        collectionApi.initiatePaymentWithHttpInfo(
            ACCEPT,
            CONTENT_TYPE,
            X_COUNTRY,
            X_CURRENCY,
            aBearerToken(),
            aPaymentRequest(MSISDN_SUCCESS));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertNotNull(result.getBody().getData().getTransaction());
    assertEquals(txId, result.getBody().getData().getTransaction().getId());
    assertTrue(result.getBody().getStatus().getSuccess());
  }

  // ─── initiatePaymentWithResponseSpec ─────────────────────────────────────

  @Test
  void initiate_payment_with_response_spec_returns_raw_response_spec_instance() {
    stubInvokeAPI();

    var result =
        collectionApi.initiatePaymentWithResponseSpec(
            ACCEPT,
            CONTENT_TYPE,
            X_COUNTRY,
            X_CURRENCY,
            aBearerToken(),
            aPaymentRequest(MSISDN_SUCCESS));

    assertNotNull(result);
    assertSame(responseSpec, result);
  }

  @Test
  void initiate_payment_propagates_401_when_bearer_token_is_invalid() {
    var ex =
        new RestClientResponseException(
            "Unauthorized",
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            null,
            null,
            null);

    stubInvokeAPI();
    doThrow(ex).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                collectionApi.initiatePayment(
                    ACCEPT,
                    CONTENT_TYPE,
                    X_COUNTRY,
                    X_CURRENCY,
                    "Bearer invalid",
                    aPaymentRequest(MSISDN_SUCCESS)));

    assertEquals(HttpStatus.UNAUTHORIZED.value(), thrown.getStatusCode().value());
  }

  @Test
  void initiate_payment_propagates_429_on_rate_limit_exceeded() {
    var ex =
        new RestClientResponseException(
            "Too Many Requests",
            HttpStatus.TOO_MANY_REQUESTS.value(),
            HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
            null,
            null,
            null);

    stubInvokeAPI();
    doThrow(ex).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                collectionApi.initiatePayment(
                    ACCEPT,
                    CONTENT_TYPE,
                    X_COUNTRY,
                    X_CURRENCY,
                    aBearerToken(),
                    aPaymentRequest(MSISDN_SUCCESS)));

    assertEquals(429, thrown.getStatusCode().value());
  }

  @Test
  void initiate_payment_propagates_500_on_internal_server_error() {
    var ex =
        new RestClientResponseException(
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            null,
            null,
            null);

    stubInvokeAPI();
    doThrow(ex).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                collectionApi.initiatePayment(
                    ACCEPT,
                    CONTENT_TYPE,
                    X_COUNTRY,
                    X_CURRENCY,
                    aBearerToken(),
                    aPaymentRequest(MSISDN_SUCCESS)));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), thrown.getStatusCode().value());
  }

  @Test
  void set_api_client_replaces_the_underlying_client_correctly() {
    var newClient = mock(ApiClient.class);
    collectionApi.setApiClient(newClient);
    assertSame(newClient, collectionApi.getApiClient());
  }
}
