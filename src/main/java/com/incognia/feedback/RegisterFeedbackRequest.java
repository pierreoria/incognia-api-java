package com.incognia.feedback;

import com.incognia.common.PersonID;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterFeedbackRequest {
  String feedbackEvent;
  String installationId;
  String sessionToken;
  Instant occurredAt;
  String requestToken;
  String loginId;
  String paymentId;
  String signupId;
  String accountId;
  String externalId;
  Instant expiresAt;
  PersonID personId;
  @Builder.Default boolean dryRun = false;
}
