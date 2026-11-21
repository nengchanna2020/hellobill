package com.hellobill.dashboard;

import com.hellobill.common.Money;
import com.hellobill.dashboard.DashboardResponse.CurrencyAmount;
import com.hellobill.invoice.Invoice;
import com.hellobill.invoice.InvoiceRepository;
import com.hellobill.invoice.InvoiceService;
import com.hellobill.invoice.InvoiceStatus;
import com.hellobill.invoice.InvoiceSummary;
import com.hellobill.payment.Payment;
import com.hellobill.payment.PaymentRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

  private final InvoiceRepository invoices;
  private final PaymentRepository payments;
  private final InvoiceService invoiceService;
  private final Clock clock;

  public DashboardService(
      InvoiceRepository invoices,
      PaymentRepository payments,
      InvoiceService invoiceService,
      Clock clock) {
    this.invoices = invoices;
    this.payments = payments;
    this.invoiceService = invoiceService;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public DashboardResponse summary() {
    List<Invoice> all = invoices.findAllByOrderByIdDesc();

    Map<String, Long> counts = new LinkedHashMap<>();
    for (InvoiceStatus status : InvoiceStatus.values()) {
      counts.put(status.name(), 0L);
    }

    Map<String, Totals> outstanding = new TreeMap<>();
    Map<String, Totals> overdue = new TreeMap<>();
    List<InvoiceSummary> recent = new ArrayList<>();

    for (Invoice invoice : all) {
      counts.merge(invoice.getStatus().name(), 1L, Long::sum);
      InvoiceSummary summary = invoiceService.toSummary(invoice);
      if (recent.size() < 5) {
        recent.add(summary);
      }
      if (invoice.getStatus() == InvoiceStatus.SENT
          || invoice.getStatus() == InvoiceStatus.OVERDUE) {
        outstanding
            .computeIfAbsent(invoice.getCurrency(), c -> new Totals())
            .add(summary.balanceDue());
      }
      if (invoice.getStatus() == InvoiceStatus.OVERDUE) {
        overdue.computeIfAbsent(invoice.getCurrency(), c -> new Totals()).add(summary.balanceDue());
      }
    }

    LocalDate today = LocalDate.now(clock);
    Map<String, Totals> paid = new TreeMap<>();
    for (Payment p : payments.findByPaidOnBetween(today.withDayOfMonth(1), today)) {
      paid.computeIfAbsent(p.getInvoice().getCurrency(), c -> new Totals()).add(p.getAmount());
    }

    return new DashboardResponse(
        counts, toList(outstanding), toList(overdue), toList(paid), recent);
  }

  private static List<CurrencyAmount> toList(Map<String, Totals> byCurrency) {
    return byCurrency.entrySet().stream()
        .map(e -> new CurrencyAmount(e.getKey(), e.getValue().amount, e.getValue().count))
        .toList();
  }

  private static final class Totals {
    BigDecimal amount = Money.ZERO;
    long count;

    void add(BigDecimal value) {
      amount = amount.add(value);
      count++;
    }
  }
}
