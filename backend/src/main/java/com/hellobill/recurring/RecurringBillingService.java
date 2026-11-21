package com.hellobill.recurring;

import com.hellobill.common.NotFoundException;
import com.hellobill.customer.Customer;
import com.hellobill.customer.CustomerService;
import com.hellobill.invoice.InvoiceItemRequest;
import com.hellobill.invoice.InvoiceRequest;
import com.hellobill.invoice.InvoiceResponse;
import com.hellobill.invoice.InvoiceService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecurringBillingService {

  private final RecurringPlanRepository plans;
  private final CustomerService customers;
  private final InvoiceService invoices;
  private final Clock clock;

  public RecurringBillingService(
      RecurringPlanRepository plans,
      CustomerService customers,
      InvoiceService invoices,
      Clock clock) {
    this.plans = plans;
    this.customers = customers;
    this.invoices = invoices;
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
                customer, request.description().trim(), request.amount(), taxRate, start));
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
   * on by one month. Drafts are not emailed: someone checks and sends them.
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
              30,
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
      created.add(invoices.get(invoice.id()));
    }
    return created;
  }
}
