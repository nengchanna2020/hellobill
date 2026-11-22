package com.hellobill.recurring;

import com.hellobill.common.NotFoundException;
import com.hellobill.customer.Customer;
import com.hellobill.customer.CustomerService;
import com.hellobill.invoice.Invoice;
import com.hellobill.invoice.InvoiceItemRequest;
import com.hellobill.invoice.InvoiceRequest;
import com.hellobill.invoice.InvoiceResponse;
import com.hellobill.invoice.InvoiceService;
import com.hellobill.invoice.InvoiceStatus;
import com.hellobill.mail.InvoiceMailer;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecurringBillingService {

  private static final Logger log = LoggerFactory.getLogger(RecurringBillingService.class);

  private final RecurringPlanRepository plans;
  private final CustomerService customers;
  private final InvoiceService invoices;
  private final InvoiceMailer mailer;
  private final Clock clock;

  public RecurringBillingService(
      RecurringPlanRepository plans,
      CustomerService customers,
      InvoiceService invoices,
      InvoiceMailer mailer,
      Clock clock) {
    this.plans = plans;
    this.customers = customers;
    this.invoices = invoices;
    this.mailer = mailer;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public List<RecurringPlanResponse> list() {
    return plans.findAllByOrderByIdAsc().stream().map(RecurringPlanResponse::from).toList();
  }

  @Transactional
  public RecurringPlanResponse create(RecurringPlanRequest request) {
    Customer customer = customers.find(request.customerId());
    LocalDate start =
        request.startDate() != null
            ? request.startDate()
            : LocalDate.now(clock).withDayOfMonth(1).plusMonths(1);
    BigDecimal taxRate = request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO;
    RecurringPlan plan =
        plans.save(
            new RecurringPlan(
                customer,
                request.description().trim(),
                request.amount(),
                taxRate,
                start,
                Boolean.TRUE.equals(request.autoSend())));
    return RecurringPlanResponse.from(plan);
  }

  @Transactional
  public RecurringPlanResponse setAutoSend(Long id, boolean autoSend) {
    RecurringPlan plan =
        plans.findById(id).orElseThrow(() -> new NotFoundException("Recurring plan", id));
    plan.setAutoSend(autoSend);
    return RecurringPlanResponse.from(plan);
  }

  @Transactional
  public RecurringPlanResponse setActive(Long id, boolean active) {
    RecurringPlan plan =
        plans.findById(id).orElseThrow(() -> new NotFoundException("Recurring plan", id));
    plan.setActive(active);
    return RecurringPlanResponse.from(plan);
  }

  /**
   * Creates a draft invoice for every active plan whose next run date has come, and moves each plan
   * on by one month. Invoices of auto-send plans are emailed and marked SENT; other drafts wait for
   * someone to check and send them.
   */
  @Transactional
  public List<InvoiceResponse> runDuePlans() {
    LocalDate today = LocalDate.now(clock);
    List<InvoiceResponse> created = new ArrayList<>();
    for (RecurringPlan plan : plans.findByActiveTrueAndNextRunDateLessThanEqual(today)) {
      LocalDate runDate = plan.getNextRunDate();
      String period =
          runDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
              + " "
              + runDate.getYear();
      InvoiceRequest request =
          new InvoiceRequest(
              plan.getCustomer().getId(),
              runDate,
              null,
              plan.getTaxRate(),
              BigDecimal.ZERO,
              "Monthly billing for " + period,
              List.of(
                  new InvoiceItemRequest(
                      plan.getDescription() + " (" + period + ")",
                      BigDecimal.ONE,
                      plan.getAmount())));
      InvoiceResponse invoice = invoices.create(request);
      invoices.find(invoice.id()).setRecurringPlanId(plan.getId());
      plan.setNextRunDate(runDate.plusMonths(1));
      if (plan.isAutoSend()) {
        trySend(invoice.id());
      }
      created.add(invoices.get(invoice.id()));
    }
    return created;
  }

  /**
   * Emails the invoice and marks it SENT. This calls the mailer directly, not invoices.send(): a
   * mail failure must not mark the whole billing transaction rollback-only and lose the other
   * plans. On failure the invoice stays a draft, to be sent by hand.
   */
  private void trySend(Long invoiceId) {
    try {
      mailer.sendInvoice(invoices.get(invoiceId));
    } catch (RuntimeException e) {
      log.error("Could not email invoice {}; it stays a draft", invoiceId, e);
      return;
    }
    Invoice invoice = invoices.find(invoiceId);
    invoice.setStatus(InvoiceStatus.SENT);
    invoice.setSentAt(Instant.now(clock));
  }
}
