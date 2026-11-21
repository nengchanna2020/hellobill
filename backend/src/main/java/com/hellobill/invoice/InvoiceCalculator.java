package com.hellobill.invoice;

import com.hellobill.common.Money;
import java.math.BigDecimal;
import java.util.List;

/** Works out the money on an invoice. Every amount is rounded to 2 decimal places. */
public final class InvoiceCalculator {

  private InvoiceCalculator() {}

  public static BigDecimal lineTotal(InvoiceItem item) {
    return Money.round(item.getQuantity().multiply(item.getUnitPrice()));
  }

  public static InvoiceTotals totals(Invoice invoice) {
    return totals(invoice.getItems(), invoice.getDiscountPercent(), invoice.getTaxRate());
  }

  public static InvoiceTotals totals(
      List<InvoiceItem> items, BigDecimal discountPercent, BigDecimal taxRate) {
    BigDecimal subtotal = Money.ZERO;
    for (InvoiceItem item : items) {
      subtotal = subtotal.add(lineTotal(item));
    }

    BigDecimal discount = Money.percentOf(subtotal, discountPercent);
    BigDecimal taxable = subtotal.subtract(discount);
    BigDecimal tax = Money.percentOf(taxable, taxRate);
    BigDecimal total = taxable.add(tax);

    return new InvoiceTotals(subtotal, discount, tax, total);
  }
}
