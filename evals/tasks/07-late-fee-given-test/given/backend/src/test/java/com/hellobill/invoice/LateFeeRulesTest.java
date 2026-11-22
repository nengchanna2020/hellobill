package com.hellobill.invoice;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LateFeeRulesTest {

  private static BigDecimal fee(String total, long daysLate) {
    return InvoiceCalculator.lateFee(new BigDecimal(total), daysLate);
  }

  @Test
  void noFeeBeforeTheDueDate() {
    assertThat(fee("100.00", 0)).isEqualByComparingTo("0.00");
  }

  @Test
  void noFeeInTheFirstMonth() {
    assertThat(fee("100.00", 1)).isEqualByComparingTo("0.00");
    assertThat(fee("100.00", 29)).isEqualByComparingTo("0.00");
  }

  @Test
  void twoPercentPerMonthAfterThat() {
    assertThat(fee("100.00", 31)).isEqualByComparingTo("2.00");
    assertThat(fee("100.00", 61)).isEqualByComparingTo("4.00");
  }

  @Test
  void atMostTenPercent() {
    assertThat(fee("100.00", 400)).isEqualByComparingTo("10.00");
  }
}
