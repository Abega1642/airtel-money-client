package dev.razafindratelo.airtel_money_client.client;

import static java.util.UUID.randomUUID;

import dev.razafindratelo.airtel_money_client.model.PaymentInitiationData;
import dev.razafindratelo.airtel_money_client.model.PaymentInitiationResponse;
import dev.razafindratelo.airtel_money_client.model.PaymentInitiationTransactionData;
import dev.razafindratelo.airtel_money_client.model.PaymentRequest;
import dev.razafindratelo.airtel_money_client.model.PaymentSubscriber;
import dev.razafindratelo.airtel_money_client.model.PaymentTransaction;
import dev.razafindratelo.airtel_money_client.model.StatusEnvelope;
import dev.razafindratelo.airtel_money_client.model.TokenRequest;
import dev.razafindratelo.airtel_money_client.model.TokenResponse;
import dev.razafindratelo.airtel_money_client.model.TransactionEnquiryData;
import dev.razafindratelo.airtel_money_client.model.TransactionEnquiryResponse;
import dev.razafindratelo.airtel_money_client.model.TransactionEnquiryTransactionData;
import dev.razafindratelo.airtel_money_client.model.TransactionStatus;
import java.math.BigDecimal;

public final class AirtelMoneyClientTestFixtures {

  private AirtelMoneyClientTestFixtures() {}

  public static final String CONTENT_TYPE = "application/json";
  public static final String ACCEPT = "application/json";
  public static final String X_COUNTRY = "MG";
  public static final String X_CURRENCY = "MGA";

  public static final String MSISDN_SUCCESS = "330000001";
  public static final String MSISDN_FAILURE = "330000002";
  public static final String MSISDN_PENDING = "330000003";

  public static TokenRequest aTokenRequest() {
    var req = new TokenRequest();
    req.setClientId(randomUUID().toString());
    req.setClientSecret(randomUUID().toString());
    req.setGrantType(TokenRequest.GrantTypeEnum.CLIENT_CREDENTIALS);
    return req;
  }

  public static TokenResponse aTokenResponse() {
    var res = new TokenResponse();
    res.setAccessToken(randomUUID().toString());
    res.setExpiresIn("3600");
    res.setTokenType(TokenResponse.TokenTypeEnum.BEARER);
    return res;
  }

  public static String aBearerToken() {
    return String.format("Bearer %s", randomUUID());
  }

  public static PaymentRequest aPaymentRequest(String msisdn) {
    var subscriber = new PaymentSubscriber();
    subscriber.setMsisdn(msisdn);
    subscriber.setCountry(X_COUNTRY);
    subscriber.setCurrency(X_CURRENCY);

    var transaction = new PaymentTransaction();
    transaction.setId(randomUUID().toString());
    transaction.setAmount(BigDecimal.valueOf(1000));
    transaction.setCountry(X_COUNTRY);
    transaction.setCurrency(X_CURRENCY);

    var req = new PaymentRequest();
    req.setReference(randomUUID().toString());
    req.setSubscriber(subscriber);
    req.setTransaction(transaction);
    return req;
  }

  public static PaymentInitiationResponse aPaymentInitiationResponse(
      String transactionId, String status) {
    var txData = new PaymentInitiationTransactionData();
    txData.setId(transactionId);
    txData.setStatus(status);

    var data = new PaymentInitiationData();
    data.setTransaction(txData);

    var statusEnvelope = aSuccessStatusEnvelope();

    var res = new PaymentInitiationResponse();
    res.setData(data);
    res.setStatus(statusEnvelope);
    return res;
  }

  public static TransactionEnquiryResponse aTransactionEnquiryResponse(
      String transactionId, TransactionStatus status) {
    var txData = new TransactionEnquiryTransactionData();
    txData.setId(transactionId);
    txData.setAirtelMoneyId(randomUUID().toString());
    txData.setStatus(status);
    txData.setMessage(status.getValue());

    var data = new TransactionEnquiryData();
    data.setTransaction(txData);

    var res = new TransactionEnquiryResponse();
    res.setData(data);
    res.setStatus(aSuccessStatusEnvelope());
    return res;
  }

  public static StatusEnvelope aSuccessStatusEnvelope() {
    var statusEnv = new StatusEnvelope();
    statusEnv.setCode("200");
    statusEnv.setMessage("SUCCESS");
    statusEnv.setSuccess(true);
    statusEnv.setResponseCode("DP00800001001");
    return statusEnv;
  }

  public static StatusEnvelope aFailureStatusEnvelope(String code, String message) {
    var statusEnv = new StatusEnvelope();
    statusEnv.setCode(code);
    statusEnv.setMessage(message);
    statusEnv.setSuccess(false);
    statusEnv.setResponseCode("DP00800001024");
    return statusEnv;
  }
}
