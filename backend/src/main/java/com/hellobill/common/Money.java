package com.hellobill.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money helpers. All amounts are BigDecimal with 2 decimal places, rounded half up. Never use
 * double for money.
 */
public final class Money {

  public static final int SCALE = 2;
  public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);
  private static final BigDecimal HUNDRED = new BigDecimal("100");

  private Money() {}

  public static BigDecimal round(BigDecimal amount) {
    return amount.setScale(SCALE, RoundingMode.HALF_UP);
  }

  /** {@code percent} of {@code amount}, for example percentOf(200, 10) = 20.00. */
  public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
    return round(amount.multiply(percent).divide(HUNDRED, 10, RoundingMode.HALF_UP));
  }
}
