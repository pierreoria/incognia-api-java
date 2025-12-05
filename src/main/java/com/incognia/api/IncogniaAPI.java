package com.incognia.api;

import com.incognia.api.clients.TokenAwareNetworkingClient;
import com.incognia.common.Address;
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

  /**
   * Registers a new signup for the given request token and address. Check <a
   * href="https://dash.incognia.com/api-reference#operation/signup-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *      Address address = Address address =
   *         Address.builder()
   *             .structuredAddress(
   *                 StructuredAddress.builder()
   *                     .countryCode("US")
   *                     .countryName("United States of America")
   *                     .locale("en-US")
   *                     .state("NY")
   *                     .city("New York City")
   *                     .borough("Manhattan")
   *                     .neighborhood("Midtown")
   *                     .street("W 34th St.")
   *                     .number("20")
   *                     .complements("Floor 2")
   *                     .postalCode("10001")
   *                     .build())
   *             .coordinates(new Coordinates(40.74836007062138, -73.98509720487937))
   *             .build();
   *      RegisterSignupRequest signupRequest = RegisterSignupRequest.builder().requestToken(requestToken).address(address).build();
   *      SignupAssessment assessment = api.registerSignup(signupRequest);
   * } catch (IncogniaAPIException e) {
   *      //Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *      //Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterSignupRequest} model that contains the properties we need to
   *     make an assessment.
   * @return the assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public SignupAssessment registerSignup(RegisterSignupRequest request) throws IncogniaException {
    Asserts.assertNotNull(request, "register signup request");
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

  /**
   * Registers a new transaction for the given request. Check <a
   * href="https://dash.incognia.com/api-reference#operation/transaction-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *   Map<AddressType, Address> addresses =
   *       Map.of(
   *           AddressType.SHIPPING,
   *               Address.builder()
   *                   .structuredAddress(
   *                       StructuredAddress.builder()
   *                           .countryCode("US")
   *                           .countryName("United States")
   *                           .locale("en-US")
   *                           .state("CA")
   *                           .city("San Francisco")
   *                           .street("Main St")
   *                           .number("123")
   *                           .postalCode("94105")
   *                           .build())
   *                   .coordinates(new Coordinates(37.7749, -122.4194))
   *                   .build());
   *
   *   RegisterTransactionRequest transactionRequest =
   *       RegisterTransactionRequest.builder()
   *           .installationId("installation-id")
   *           .requestToken("request-token")
   *           .policyId("policy-id")
   *           .appVersion("1.2.3")
   *           .deviceOs("android")
   *           .accountId("user-account-001")
   *           .externalId("ext-user-id-789")
   *           .storeId("store-987")
   *           .type("payment") // e.g. "login" or "payment"
   *           .addresses(addresses)
   *           .customProperties(Map.of("key", "value"))
   *           .personId(PersonID.ofCPF("11725849070"))
   *           // Optional: evaluation query parameter (eval=true/false)
   *           .shouldEvaluateTransaction(true)
   *           .build();
   *
   *   TransactionAssessment assessment = api.registerTransaction(transactionRequest);
   * } catch (IncogniaAPIException e) {
   *   // Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *   // Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterTransactionRequest} model that contains the properties we need
   *     to make an assessment.
   * @return the assessment
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public TransactionAssessment registerTransaction(RegisterTransactionRequest request)
      throws IncogniaException {
    Asserts.assertNotNull(request, "register transaction request");
    Asserts.assertNotEmpty(request.getAccountId(), "account id");
    Asserts.assertNotEmpty(request.getType(), "type");
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

  /**
   * Registers a new feedback event (e.g. marking a previous assessment as fraud/legit/reset). Check <a
   * href="https://dash.incognia.com/api-reference#operation/feedback-post">the docs</a><br>
   * Example:
   *
   * <pre>{@code
   * IncogniaAPI api = IncogniaAPI.init("client-id", "client-secret");
   * try {
   *   Instant now = Instant.now();
   *   Instant expiresAt = now.plus(1, ChronoUnit.DAYS);
   *
   *   RegisterFeedbackRequest feedbackRequest =
   *       RegisterFeedbackRequest.builder()
   *           .feedbackEvent(FeedbackEvent.RESET)
   *           .occurredAt(now)
   *           .installationId("installation-id")   // optional, but recommended when available
   *           .sessionToken("session-token")       // optional
   *           .accountId("user-account-001")       // optional, depending on your identifiers
   *           .externalId("ext-user-id-789")       // optional
   *           .requestToken("request-token")       // optional
   *           .signupId("signup-id-123")           // optional - set the identifier that matches the event target
   *           .loginId("login-id-456")             // optional
   *           .paymentId("payment-id-789")         // optional
   *           .personId(PersonID.ofCPF("11725849070")) // optional
   *           .expiresAt(expiresAt)                // optional (used by some feedback events)
   *           .build();
   *
   *   // dryRun=true validates/registers without persisting the feedback on the server
   *   api.registerFeedback(feedbackRequest, false);
   * } catch (IncogniaAPIException e) {
   *   // Some api error happened (invalid data, invalid credentials)
   * } catch (IncogniaException e) {
   *   // Something unexpected happened
   * }
   * }</pre>
   *
   * @param request the {@link RegisterFeedbackRequest} model that contains the feedback event and the
   *     identifiers used to associate it with a previous assessment.
   * @param dryRun whether the request should be executed in dry-run mode (sent with the {@code dry_run}
   *     query parameter).
   * @throws IncogniaAPIException in case of api errors
   * @throws IncogniaException in case of unexpected errors
   */
  public void registerFeedback(RegisterFeedbackRequest request, boolean dryRun)
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
    queryParameters.put(DRY_RUN_PARAMETER, String.valueOf(dryRun));
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
