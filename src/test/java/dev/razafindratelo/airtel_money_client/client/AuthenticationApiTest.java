package dev.razafindratelo.airtel_money_client.client;

import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.ACCEPT;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.CONTENT_TYPE;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aTokenRequest;
import static dev.razafindratelo.airtel_money_client.client.AirtelMoneyClientTestFixtures.aTokenResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.razafindratelo.airtel_money_client.api.AuthenticationApi;
import dev.razafindratelo.airtel_money_client.invoker.ApiClient;
import dev.razafindratelo.airtel_money_client.model.TokenRequest;
import dev.razafindratelo.airtel_money_client.model.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class AuthenticationApiTest {

  @Mock private ApiClient apiClient;
  @Mock private ResponseSpec responseSpec;

  private AuthenticationApi authenticationApi;

  @BeforeEach
  void setUp() {
    authenticationApi = new AuthenticationApi(apiClient);
  }

  private void stubInvokeAPI() {
    doReturn(responseSpec)
        .when(apiClient)
        .invokeAPI(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void get_access_token_returns_token_response_with_correct_fields() {
    var request = aTokenRequest();
    var expected = aTokenResponse();

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = authenticationApi.getAccessToken(CONTENT_TYPE, ACCEPT, request);

    assertNotNull(result);
    assertEquals(expected.getAccessToken(), result.getAccessToken());
    assertEquals(expected.getExpiresIn(), result.getExpiresIn());
    assertEquals(TokenResponse.TokenTypeEnum.BEARER, result.getTokenType());
  }

  @Test
  void get_access_token_token_type_is_always_bearer() {
    var expected = aTokenResponse();

    stubInvokeAPI();
    doReturn(expected).when(responseSpec).body(any(ParameterizedTypeReference.class));

    var result = authenticationApi.getAccessToken(CONTENT_TYPE, ACCEPT, aTokenRequest());

    assertEquals(TokenResponse.TokenTypeEnum.BEARER, result.getTokenType());
  }

  @Test
  void get_access_token_grant_type_is_always_client_credentials() {
    assertEquals(TokenRequest.GrantTypeEnum.CLIENT_CREDENTIALS, aTokenRequest().getGrantType());
  }

  @Test
  void get_access_token_with_http_info_returns_response_entity_wrapping_token_response() {
    var expected = aTokenResponse();
    var entity = ResponseEntity.ok(expected);

    stubInvokeAPI();
    doReturn(entity).when(responseSpec).toEntity(any(ParameterizedTypeReference.class));

    var result =
        authenticationApi.getAccessTokenWithHttpInfo(CONTENT_TYPE, ACCEPT, aTokenRequest());

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(expected.getAccessToken(), result.getBody().getAccessToken());
    assertEquals(expected.getExpiresIn(), result.getBody().getExpiresIn());
    assertEquals(TokenResponse.TokenTypeEnum.BEARER, result.getBody().getTokenType());
  }

  @Test
  void get_access_token_with_response_spec_returns_raw_response_spec() {
    stubInvokeAPI();

    var result =
        authenticationApi.getAccessTokenWithResponseSpec(CONTENT_TYPE, ACCEPT, aTokenRequest());

    assertNotNull(result);
    assertSame(responseSpec, result);
  }

  @Test
  void get_access_token_propagates_rest_client_response_exception_on_401() {
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
            () -> authenticationApi.getAccessToken(CONTENT_TYPE, ACCEPT, aTokenRequest()));

    assertEquals(HttpStatus.UNAUTHORIZED.value(), thrown.getStatusCode().value());
  }

  @Test
  void get_access_token_uses_correct_endpoint_path() {
    stubInvokeAPI();
    doReturn(aTokenResponse()).when(responseSpec).body(any(ParameterizedTypeReference.class));

    authenticationApi.getAccessToken(CONTENT_TYPE, ACCEPT, aTokenRequest());

    verify(apiClient)
        .invokeAPI(
            eq("/auth/oauth2/token"),
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
  void get_access_token_api_client_is_replaced_correctly_via_setter() {
    var newClient = mock(ApiClient.class);
    authenticationApi.setApiClient(newClient);
    assertSame(newClient, authenticationApi.getApiClient());
  }
}
