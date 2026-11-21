package com.hellobill.dashboard;

import com.hellobill.invoice.InvoiceSummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Amounts are grouped by currency, because you can't add dollars to euros. */
public record DashboardResponse(
    Map<String, Long> counts,
    List<CurrencyAmount> outstanding,
    List<CurrencyAmount> overdue,
    List<CurrencyAmount> paidThisMonth,
    List<InvoiceSummary> recentInvoices) {

  public record CurrencyAmount(String currency, BigDecimal amount, long invoices) {}
}
