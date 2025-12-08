package com.incognia.api;

import com.incognia.api.clients.TokenAwareNetworkingClient;
import com.incognia.common.Address;
import com.incognia.common.Location;
import com.incognia.common.exceptions.IncogniaAPIException;
import com.incognia.common.exceptions.IncogniaException;
import com.incognia.common.utils.Asserts;
import com.incognia.common.utils.ClientCredentials;
import com.incognia.common.utils.CustomOptions;
import com.incognia.feedback.FeedbackEvent;
import com.incognia.feedback.RegisterFeedbackRequest;
import com.incognia.feedback.PostFeedbackRequestBody;
import com.incognia.onboarding.PostSignupRequestBody;
import com.incognia.onboarding.RegisterSignupRequest;
import com.incognia.onboarding.SignupAssessment;
import com.incognia.transaction.AddressType;
import com.incognia.transaction.RegisterTransactionRequest;
import com.incognia.transaction.PostTransactionRequestBody;
import com.incognia.transaction.TransactionAddress;
import com.incognia.transaction.TransactionAssessment;
import com.incognia.transaction.login.RegisterLoginRequest;
import com.incognia.transaction.payment.RegisterPaymentRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

/**
 * Class providing an implementation of the API endpoints described in <a
 * href="https://dash.incognia.com/api-reference">API reference</a>.
 *
 * <p>Automatically handles token generation and renewal.
 */
public class IncogniaAPI {
  private static final String API_URL = "https://api.incognia.com";
  private static final String EVALUATION_PARAMETER = "eval";
  private static final String DRY_RUN_PARAMETER = "dry_run";

  private final TokenAwareNetworkingClient tokenAwareNetworkingClient;

  private static final ConcurrentHashMap<ClientCredentials, IncogniaAPI> INSTANCES =
      new ConcurrentHashMap<>();

  /**
   * Creates a new instance for a given client id/secret.
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   */
  IncogniaAPI(String clientId, String clientSecret, CustomOptions options) {
    this(clientId, clientSecret, options, API_URL);
  }

  IncogniaAPI(String clientId, String clientSecret, CustomOptions options, String apiUrl) {
    Asserts.assertNotEmpty(clientId, "client id");
    Asserts.assertNotEmpty(clientSecret, "client secret");
    Asserts.assertNotEmpty(apiUrl, "api url");
    tokenAwareNetworkingClient =
        new TokenAwareNetworkingClient(
            new OkHttpClient.Builder()
                .callTimeout(options.getTimeoutMillis(), TimeUnit.MILLISECONDS)
                .connectionPool(
                    new ConnectionPool(
                        options.getMaxConnections(),
                        options.getKeepAliveSeconds(),
                        TimeUnit.SECONDS))
                .build(),
            apiUrl,
            clientId,
            clientSecret);
  }

  /**
   * Initializes a IncogniaAPI instance for a given client id/secret and returns it
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   * @param options custom options that can be passed to the library
   * @return the IncogniaAPI instance
   */
  public static IncogniaAPI init(String clientId, String clientSecret, CustomOptions options) {
    ClientCredentials credentials =
        ClientCredentials.builder().clientId(clientId).clientSecret(clientSecret).build();

    INSTANCES.computeIfAbsent(credentials, c -> new IncogniaAPI(clientId, clientSecret, options));
    return INSTANCES.get(credentials);
  }

  /**
   * Initializes a IncogniaAPI instance for a given client id/secret and returns it
   *
   * @param clientId the client id
   * @param clientSecret the client secret
   * @return the IncogniaAPI instance
   */
  public static IncogniaAPI init(String clientId, String clientSecret) {
    return init(clientId, clientSecret, CustomOptions.builder().build());
  }

  /**
   * If there is only one IncogniaAPI instance in the multiton, return that instance
   *
   * @return the single IncogniaAPI instance
   * @throws IllegalStateException if more than one instance exists or if no instance exists
   */
  public static IncogniaAPI instance() {
    if (INSTANCES.isEmpty()) {
      throw new IllegalStateException(
          "No API instance has been created. Use IncogniaAPI.init(clientId, clientSecret) to create one");
    } else if (INSTANCES.size() > 1) {
      throw new IllegalStateException(
          "Multiple IncogniaAPI instances have been created. Use IncogniaAPI.instance(clientId, clientSecret) to retrieve the desired one.");
    }

    return INSTANCES.entrySet().iterator().next().getValue();
  }

  /**
   * Returns the instance of IncogniaAPI for a given client id/secret if it was initialized using
   * {@link #init(String, String)}
   *
   * @return the IncogniaAPI instance
   * @throws IllegalStateException if no instance has been initialized for the given client
   *     id/secret
   */
  public static IncogniaAPI instance(String clientId, String clientSecret) {
    ClientCredentials credentials =
        ClientCredentials.builder().clientId(clientId).clientSecret(clientSecret).build();

    if (!INSTANCES.containsKey(credentials)) {
      throw new IllegalStateException(
          "IncogniaAPI instance not initialized. Use IncogniaAPI.init(clientId, clientSecret) to set it.");
    }
    return INSTANCES.get(credentials);
  }

 
  public SignupAssessment registerSignup(RegisterSignupRequest request) throws IncogniaException {
    Asserts.assertNotNull(request, "register signup request");
    Asserts.assertNotNull(request.getPolicyId(), "policy id");
    Optional<Address> address = Optional.ofNullable(request.getAddress());
    PostSignupRequestBody postSignupRequestBody =
        PostSignupRequestBody.builder()
            .installationId(request.getInstallationId())
            .requestToken(request.getRequestToken())
            .sessionToken(request.getSessionToken())
            .appVersion(request.getAppVersion())
            .deviceOs(
                Optional.ofNullable(request.getDeviceOs()).map(String::toLowerCase).orElse(null))
            .addressLine(address.map(Address::getAddressLine).orElse(null))
            .structuredAddress(address.map(Address::getStructuredAddress).orElse(null))
            .addressCoordinates(address.map(Address::getCoordinates).orElse(null))
            .externalId(request.getExternalId())
            .policyId(request.getPolicyId())
            .accountId(request.getAccountId())
            .additionalLocations(request.getAdditionalLocations())
            .customProperties(request.getCustomProperties())
            .personId(request.getPersonId())
            .build();
    return tokenAwareNetworkingClient.doPost(
        "api/v2/onboarding/signups", postSignupRequestBody, SignupAssessment.class);
  }

  
  public TransactionAssessment registerTransaction(RegisterTransactionRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register transaction request");
    Asserts.assertNotEmpty(request.getAccountId(), "account id");
    Asserts.assertNotEmpty(request.getType(), "type");
    Asserts.assertNotNull(request.getPolicyId(), "policy id");
    List<TransactionAddress> transactionAddresses =
        addressMapToTransactionAddresses(request.getAddresses());
    PostTransactionRequestBody requestBody =
        PostTransactionRequestBody.builder()
            .installationId(request.getInstallationId())
            .requestToken(request.getRequestToken())
            .appVersion(request.getAppVersion())
            .location(request.getLocation())
            .deviceOs(
                Optional.ofNullable(request.getDeviceOs()).map(String::toLowerCase).orElse(null))
            .accountId(request.getAccountId())
            .externalId(request.getExternalId())
            .policyId(request.getPolicyId())
            .relatedAccountId(request.getRelatedAccountId())
            .customProperties(request.getCustomProperties())
            .personId(request.getPersonId())
            .storeId(request.getStoreId())
            .coupon(request.getCoupon())
            .debtorAccount(request.getDebtorAccount())
            .creditorAccount(request.getCreditorAccount())
            .paymentValue(request.getPaymentValue())
            .paymentMethods(request.getPaymentMethods())
            .type(request.getType())
            .addresses(transactionAddresses)
            .build();

    Map<String, String> queryParameters = new HashMap<>();
    if (request.shouldEvaluateTransaction() != null) {
      queryParameters.put(EVALUATION_PARAMETER, request.shouldEvaluateTransaction().toString());
    }
    return tokenAwareNetworkingClient.doPost(
        "api/v2/authentication/transactions",
        requestBody,
        TransactionAssessment.class,
        queryParameters);
  }


  public void registerFeedback(RegisterFeedbackRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register feedback request");
    Asserts.assertNotNull(request.getFeedbackEvent(), "feedback event");
    Asserts.assertNotNull(request.getOccurredAt(), "occurred at");
    PostFeedbackRequestBody requestBody =
        PostFeedbackRequestBody.builder()
            .event(request.getFeedbackEvent())
            .occurredAt(
                Optional.ofNullable(request.getOccurredAt()).map(Instant::toString).orElse(null))
            .installationId(request.getInstallationId())
            .sessionToken(request.getSessionToken())
            .accountId(request.getAccountId())
            .loginId(request.getLoginId())
            .paymentId(request.getPaymentId())
            .signupId(request.getSignupId())
            .externalId(request.getExternalId())
            .requestToken(request.getRequestToken())
            .personId(request.getPersonId())
            .expiresAt(
                Optional.ofNullable(request.getExpiresAt()).map(Instant::toString).orElse(null))
            .build();

    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put(DRY_RUN_PARAMETER, String.valueOf(request.isDryRun()));
    tokenAwareNetworkingClient.doPost("api/v2/feedbacks", requestBody, queryParameters);
  }

  @NotNull
  private List<TransactionAddress> addressMapToTransactionAddresses(
      Map<AddressType, Address> addresses) {
    return addresses.entrySet().stream()
        .map(
            entry -> {
              Address address = entry.getValue();
              return new TransactionAddress(
                  entry.getKey().name().toLowerCase(),
                  address.getAddressLine(),
                  address.getStructuredAddress(),
                  address.getCoordinates());
            })
        .collect(Collectors.toList());
  }
}
