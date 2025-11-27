package com.incognia.transaction.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinancialAccountHolderTaxId {
  String type;
  String value;
}
