package com.incognia.transaction.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentBrand {
    @JsonProperty("amex")
    AMEX,
    @JsonProperty("argencard")
    ARGENCARD,
    @JsonProperty("cabal")
    CABAL,
    @JsonProperty("mastercard")
    MASTERCARD,
    @JsonProperty("tarjeta_naranja")
    TARJETA_NARANJA,
    @JsonProperty("visa")
    VISA;
}