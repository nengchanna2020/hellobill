package com.hellobill.invoice;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceCalculatorTest {

  private static InvoiceItem item(String qty, String price) {
    return new InvoiceItem("Item", new BigDecimal(qty), new BigDecimal(price));
  }

  private static InvoiceTotals totals(List<InvoiceItem> items, String discount, String tax) {
    return InvoiceCalculator.totals(items, new BigDecimal(discount), new BigDecimal(tax));
  }

  @Test
  void addsUpLinesWithNoTaxOrDiscount() {
    InvoiceTotals t = totals(List.of(item("2", "50.00"), item("1", "25.50")), "0", "0");

    assertThat(t.subtotal()).isEqualByComparingTo("125.50");
    assertThat(t.discount()).isEqualByComparingTo("0");
    assertThat(t.tax()).isEqualByComparingTo("0");
    assertThat(t.total()).isEqualByComparingTo("125.50");
  }

  @Test
  void addsTax() {
    InvoiceTotals t = totals(List.of(item("1", "200.00")), "0", "10");

    assertThat(t.tax()).isEqualByComparingTo("20.00");
    assertThat(t.total()).isEqualByComparingTo("220.00");
  }

  @Test
  void takesOffDiscount() {
    InvoiceTotals t = totals(List.of(item("4", "25.00")), "15", "0");

    assertThat(t.subtotal()).isEqualByComparingTo("100.00");
    assertThat(t.discount()).isEqualByComparingTo("15.00");
    assertThat(t.total()).isEqualByComparingTo("85.00");
  }

  @Test
  void taxesTheAmountAfterDiscount() {
    InvoiceTotals t = totals(List.of(item("1", "450.00")), "10", "8");

    assertThat(t.subtotal()).isEqualByComparingTo("450.00");
    assertThat(t.discount()).isEqualByComparingTo("45.00");
    assertThat(t.tax()).isEqualByComparingTo("32.40");
    assertThat(t.total()).isEqualByComparingTo("437.40");
  }

  @Test
  void roundsEachLineToCents() {
    InvoiceTotals t = totals(List.of(item("1.5", "9.99")), "0", "0");

    // 1.5 x 9.99 = 14.985, rounded half up
    assertThat(t.subtotal()).isEqualByComparingTo("14.99");
  }

  @Test
  void roundsTaxToCents() {
    InvoiceTotals t = totals(List.of(item("1", "19.99")), "0", "7.5");

    // 7.5% of 19.99 = 1.49925
    assertThat(t.tax()).isEqualByComparingTo("1.50");
    assertThat(t.total()).isEqualByComparingTo("21.49");
  }

  @Test
  void emptyInvoiceIsZero() {
    InvoiceTotals t = totals(List.of(), "0", "0");

    assertThat(t.total()).isEqualByComparingTo("0");
  }
}
