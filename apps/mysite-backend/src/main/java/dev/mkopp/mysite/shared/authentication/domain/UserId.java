package dev.mkopp.mysite.shared.authentication.domain;

import java.util.UUID;

import dev.mkopp.mysite.shared.error.domain.Assert;
import dev.mkopp.mysite.shared.error.domain.InvalidUserIdException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Represents a user's unique, immutable identifier (from the JWT 'sub' claim).
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserId {
  UUID value;

  public static UserId of(String userId) {
    Assert.notBlank("userId", userId);

    try {
      UUID uuid = UUID.fromString(userId);
      return new UserId(uuid);
    } catch (IllegalArgumentException e) {
      throw new InvalidUserIdException(userId, e);
    }
  }
}