package com.hellobill.evalcheck;

import static org.assertj.core.api.Assertions.assertThat;

import com.hellobill.invoice.InvoiceCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LateFeeCheck {

  private static BigDecimal fee(String total, long daysLate) {
    return InvoiceCalculator.lateFee(new BigDecimal(total), daysLate);
  }

  @Test
  void notLate() {
    assertThat(fee("100.00", 0)).isEqualByComparingTo("0.00");
    assertThat(fee("100.00", -3)).isEqualByComparingTo("0.00");
  }

  @Test
  void twoPercentForEachStarted30Days() {
    assertThat(fee("100.00", 1)).isEqualByComparingTo("2.00");
    assertThat(fee("100.00", 30)).isEqualByComparingTo("2.00");
    assertThat(fee("100.00", 31)).isEqualByComparingTo("4.00");
    assertThat(fee("100.00", 90)).isEqualByComparingTo("6.00");
  }

  @Test
  void atMostTenPercent() {
    assertThat(fee("100.00", 150)).isEqualByComparingTo("10.00");
    assertThat(fee("100.00", 400)).isEqualByComparingTo("10.00");
  }

  @Test
  void roundsHalfUpToCents() {
    assertThat(fee("123.45", 15)).isEqualByComparingTo("2.47");
    assertThat(fee("0.25", 29)).isEqualByComparingTo("0.01");
  }
}
