package com.incognia.common;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
public class Location {
  String latitude;
  String longitude;
  String collectedAt;

  @Builder
  public Location(String latitude, String longitude, Instant collectedAt) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.collectedAt = (collectedAt == null) ? null : collectedAt.toString();
  }
}
