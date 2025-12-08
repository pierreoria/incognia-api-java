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
