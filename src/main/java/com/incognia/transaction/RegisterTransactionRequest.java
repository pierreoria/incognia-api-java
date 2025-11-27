package com.incognia.transaction;

import com.incognia.common.Address;
import com.incognia.common.Location;
import com.incognia.common.PersonID;
import com.incognia.transaction.payment.FinancialAccount;
import com.incognia.transaction.payment.Coupon;
import com.incognia.transaction.AddressType;
import com.incognia.transaction.payment.PaymentMethod;
import com.incognia.transaction.payment.PaymentValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class RegisterTransactionRequest {
  String accountId;
  String policyId;
  String requestToken;

  String type;
  
  String externalId;
  
  String appVersion;
  String deviceOs;
  String relatedAccountId;
  String storeId;
  Location location;
  PersonID personId;
  Coupon coupon;
  FinancialAccount debtorAccount;
  FinancialAccount creditorAccount;
  PaymentValue paymentValue;
  @Builder.Default Map<AddressType, Address> addresses = Collections.emptyMap();
  @Builder.Default List<PaymentMethod> paymentMethods = Collections.emptyList();
  @Builder.Default Map<String, Object> customProperties = Collections.emptyMap();

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  String installationId;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}

//  String installationId;

//   @Deprecated String sessionToken;
//   String requestToken;

//   @Pattern(
//       regexp = RegexConstants.REGEX_NO_SPECIAL_CHARACTERS,
//       message = "account_id cannot contain special characters")
//   String accountId;

//   @Valid PersonIdDto personId;
//   @Valid FinancialAccountDto debtorAccount;
//   @Valid FinancialAccountDto creditorAccount;

//   @NotEmpty(message = "missing type")
//   @ValidEnum(message = "unsupported type", enumClass = TransactionType.class)
//   String type;

//   @Valid PaymentValueDto paymentValue;
//   @Valid CouponDto coupon;

//   @Pattern(
//       regexp = RegexConstants.REGEX_NO_SPECIAL_CHARACTERS,
//       message = "external_id contains invalid characters")
//   @Size(max = 100, message = "external_id too large")
//   String externalId;

//   @DistinctAddressTypes(message = "must not contain more than one address of same type")
//   @Valid
//   List<TransactionAddressDto> addresses;

//   @Valid List<PaymentMethodDto> paymentMethods;

//   String policyId;

//   @Valid LocationDto location;

//   String paymentMethodIdentifier;

//   @ValidCustomPropertiesTypes(message = "custom_properties contains invalid fields")
//   Map<String, Object> customProperties;

//   String storeId;

//   @Pattern(regexp = RegexConstants.REGEX_VALID_OS, message = "device_os has invalid value")
//   String deviceOs;

//   @Pattern(
//       regexp = RegexConstants.REGEX_NO_SPECIAL_CHARACTERS,
//       message = "app_version cannot contain special characters")
//   String appVersion;

//   List<
//           @Pattern(
//               regexp = RegexConstants.REGEX_TWO_UPPERCASE_CHARS,
//               message =
//                   "country codes must be in the ISO 3166-1 alpha-2 format (2 uppercase characters)")
//           String>
//       countries;
// }