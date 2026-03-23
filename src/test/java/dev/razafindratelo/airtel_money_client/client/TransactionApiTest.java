package dev.razafindratelo.airtel_money_client.client;

import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.ACCEPT;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.X_COUNTRY;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.X_CURRENCY;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aBearerToken;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aTransactionEnquiryResponse;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.razafindratelo.airtel_money_client.api.TransactionApi;
import dev.razafindratelo.airtel_money_client.invoker.ApiClient;
import dev.razafindratelo.airtel_money_client.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionApiTest {

  @Mock private ApiClient apiClient;
  @Mock private RestClient.ResponseSpec responseSpec;

  private TransactionApi subject;

  @BeforeEach
  void setUp() {
    subject = new TransactionApi(apiClient);
  }

  private void stubInvokeAPI() {
    doReturn(responseSpec)
        .when(apiClient)
        .invokeAPI(
            eq("/standard/v1/payments/{id}"),
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
  void get_transaction_status_ts_status_returns_success_response_with_airtel_money_id() {
    var txId = randomUUID().toString();
    var expected = aTransactionEnquiryResponse(txId, TransactionStatus.TS);

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result);
    assertNotNull(result.getData());
    assertNotNull(result.getStatus());
    assertNotNull(result.getData().getTransaction());
    assertEquals(txId, result.getData().getTransaction().getId());
    assertEquals(TransactionStatus.TS, result.getData().getTransaction().getStatus());
    assertNotNull(result.getData().getTransaction().getAirtelMoneyId());
    assertTrue(result.getStatus().getSuccess());
  }

  @Test
  void get_transaction_status_tf_status_returns_failure_response() {
    var txId = randomUUID().toString();
    var expected = aTransactionEnquiryResponse(txId, TransactionStatus.TF);

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result);
    assertNotNull(result.getData().getTransaction());
    assertEquals(TransactionStatus.TF, result.getData().getTransaction().getStatus());
    assertEquals("TF", result.getData().getTransaction().getMessage());
  }

  @Test
  void get_transaction_status_tip_status_means_customer_has_not_responded_yet() {
    var txId = randomUUID().toString();
    var expected = aTransactionEnquiryResponse(txId, TransactionStatus.TIP);

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result);
    assertNotNull(result.getData().getTransaction());
    assertEquals(TransactionStatus.TIP, result.getData().getTransaction().getStatus());
  }

  @Test
  void get_transaction_status_ta_status_means_transaction_is_ambiguous_and_should_be_retried() {
    var txId = randomUUID().toString();
    var expected = aTransactionEnquiryResponse(txId, TransactionStatus.TA);

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result);
    assertNotNull(result.getData().getTransaction());
    assertEquals(TransactionStatus.TA, result.getData().getTransaction().getStatus());
  }

  @Test
  void get_transaction_status_te_status_means_transaction_has_expired() {
    var txId = randomUUID().toString();
    var expected = aTransactionEnquiryResponse(txId, TransactionStatus.TE);

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result);
    assertNotNull(result.getData().getTransaction());
    assertEquals(TransactionStatus.TE, result.getData().getTransaction().getStatus());
  }

  @Test
  void get_transaction_status_uses_correct_endpoint_path_with_transaction_id() {
    var txId = randomUUID().toString();

    stubInvokeAPI();
    doReturn(aTransactionEnquiryResponse(txId, TransactionStatus.TS))
        .when(responseSpec)
        .body(any(ParameterizedTypeReference.class));

    subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    verify(apiClient)
        .invokeAPI(
            eq("/standard/v1/payments/{id}"),
            any(),
            argThat(pathParams -> txId.equals(pathParams.get("id"))),
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
  void get_transaction_status_response_partner_id_matches_the_one_sent_at_initiation() {
    var txId = randomUUID().toString();
    var expected = aTransactionEnquiryResponse(txId, TransactionStatus.TS);

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result.getData().getTransaction());
    assertEquals(txId, result.getData().getTransaction().getId());
  }

  @Test
  void get_transaction_status_with_http_info_returns_response_entity_with_200_and_body() {
    var txId = randomUUID().toString();
    var entity = ResponseEntity.ok(aTransactionEnquiryResponse(txId, TransactionStatus.TS));

    stubInvokeAPI();
    doReturn(entity).when(responseSpec).toEntity(any(ParameterizedTypeReference.class));

    var result =
        subject.getTransactionStatusWithHttpInfo(
            txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertNotNull(result.getBody().getData().getTransaction());
    assertEquals(txId, result.getBody().getData().getTransaction().getId());
    assertEquals(TransactionStatus.TS, result.getBody().getData().getTransaction().getStatus());
  }

  @Test
  void get_transaction_status_with_response_spec_returns_raw_response_spec_instance() {
    var txId = randomUUID().toString();

    stubInvokeAPI();

    var result =
        subject.getTransactionStatusWithResponseSpec(
            txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken());

    assertNotNull(result);
    assertSame(responseSpec, result);
  }

  @Test
  void get_transaction_status_propagates_404_when_transaction_id_does_not_exist() {
    var txId = randomUUID().toString();
    var ex =
        new RestClientResponseException(
            "Not Found",
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            null,
            null,
            null);

    stubInvokeAPI();
    doThrow(ex).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken()));

    assertEquals(HttpStatus.NOT_FOUND.value(), thrown.getStatusCode().value());
  }

  @Test
  void get_transaction_status_propagates_401_when_bearer_token_is_expired() {
    var txId = randomUUID().toString();
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
                subject.getTransactionStatus(
                    txId, ACCEPT, X_COUNTRY, X_CURRENCY, "Bearer expired-token"));

    assertEquals(HttpStatus.UNAUTHORIZED.value(), thrown.getStatusCode().value());
  }

  @Test
  void get_transaction_status_propagates_408_on_read_timeout() {
    var txId = randomUUID().toString();
    var ex =
        new RestClientResponseException(
            "Request Timeout",
            HttpStatus.REQUEST_TIMEOUT.value(),
            HttpStatus.REQUEST_TIMEOUT.getReasonPhrase(),
            null,
            null,
            null);

    stubInvokeAPI();
    doThrow(ex).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                subject.getTransactionStatus(txId, ACCEPT, X_COUNTRY, X_CURRENCY, aBearerToken()));

    assertEquals(HttpStatus.REQUEST_TIMEOUT.value(), thrown.getStatusCode().value());
  }

  @Test
  void set_api_client_replaces_the_underlying_client_correctly() {
    var newClient = mock(ApiClient.class);
    subject.setApiClient(newClient);
    assertSame(newClient, subject.getApiClient());
  }
}
