package com.incognia.transaction.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentMethod {
  PaymentType type;
  PaymentBrand brand;
  String identifier;
  CardInfo creditCardInfo;
  CardInfo debitCardInfo;
}
