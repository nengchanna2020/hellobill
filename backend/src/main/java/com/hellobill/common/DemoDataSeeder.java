package com.hellobill.common;

import com.hellobill.customer.CustomerRepository;
import com.hellobill.customer.CustomerRequest;
import com.hellobill.customer.CustomerResponse;
import com.hellobill.customer.CustomerService;
import com.hellobill.invoice.Invoice;
import com.hellobill.invoice.InvoiceItemRequest;
import com.hellobill.invoice.InvoiceRequest;
import com.hellobill.invoice.InvoiceResponse;
import com.hellobill.invoice.InvoiceService;
import com.hellobill.invoice.InvoiceStatus;
import com.hellobill.payment.Payment;
import com.hellobill.payment.PaymentRepository;
import com.hellobill.recurring.RecurringBillingService;
import com.hellobill.recurring.RecurringPlanRequest;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills an empty database with fake demo data: made-up businesses, example.com email addresses and
 * invoices dated around today. Turn it off with hellobill.demo-data=false.
 */
@Component
@Order(0)
@ConditionalOnProperty(name = "hellobill.demo-data", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

  private final CustomerRepository customerRepository;
  private final CustomerService customers;
  private final InvoiceService invoices;
  private final PaymentRepository payments;
  private final RecurringBillingService recurring;
  private final Clock clock;

  public DemoDataSeeder(
      CustomerRepository customerRepository,
      CustomerService customers,
      InvoiceService invoices,
      PaymentRepository payments,
      RecurringBillingService recurring,
      Clock clock) {
    this.customerRepository = customerRepository;
    this.customers = customers;
    this.invoices = invoices;
    this.payments = payments;
    this.recurring = recurring;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (customerRepository.count() > 0) {
      return;
    }
    LocalDate today = LocalDate.now(clock);

    CustomerResponse lotus =
        customer("Lotus Bakery", "accounts@lotus-bakery.example.com", "KH", "USD", 30);
    CustomerResponse kranich =
        customer("Kranich Design GmbH", "rechnung@kranich.example.com", "DE", "EUR", 14);
    CustomerResponse maple =
        customer("Maple & Pine Studio", "hello@maplepine.example.com", "CA", "CAD", 30);
    CustomerResponse sakura =
        customer("Sakura Tea House", "billing@sakura-tea.example.com", "JP", "JPY", 45);
    CustomerResponse river =
        customer("Riverbend Fitness", "office@riverbend.example.com", "US", "USD", 30);

    // Paid in full last month.
    InvoiceResponse inv1 =
        invoice(
            lotus,
            today.minusDays(40),
            "0",
            "0",
            item("Website maintenance, October", "1", "450.00"),
            item("Extra page: catering menu", "2", "75.00"));
    send(inv1, today.minusDays(40));
    pay(inv1, inv1.total(), today.minusDays(28), "BANK_TRANSFER");

    // Sent, with VAT, part paid today.
    InvoiceResponse inv2 =
        invoice(
            kranich,
            today.minusDays(10),
            "19",
            "0",
            item("Logo refresh", "1", "1200.00"),
            item("Brand guidelines PDF", "1", "350.00"));
    send(inv2, today.minusDays(10));
    pay(inv2, new BigDecimal("500.00"), today, "BANK_TRANSFER");

    // Sent long ago, partly paid, now overdue.
    InvoiceResponse inv3 =
        invoice(
            maple,
            today.minusDays(52),
            "5",
            "0",
            item("Photo shoot, half day", "1", "800.00"),
            item("Edited photos", "40", "12.50"));
    send(inv3, today.minusDays(52));
    pay(inv3, new BigDecimal("500.00"), today.minusDays(30), "CARD");

    // Sent with a discount for a loyal customer.
    InvoiceResponse inv4 =
        invoice(
            river,
            today.minusDays(3),
            "8",
            "10",
            item("Class booking app, monthly licence", "1", "300.00"),
            item("Setup for new studio", "1", "150.00"));
    send(inv4, today.minusDays(3));

    // Yen invoice: yen has no cents.
    InvoiceResponse inv5 =
        invoice(
            sakura,
            today.minusDays(5),
            "10",
            "0",
            item("Menu translation", "1", "48000"),
            item("Printed menus", "30", "350"));
    send(inv5, today.minusDays(5));

    // Still a draft.
    invoice(lotus, today, "0", "0", item("Website maintenance, this month", "1", "450.00"));

    recurring.create(
        new RecurringPlanRequest(
            lotus.id(), "Website maintenance", new BigDecimal("450.00"), BigDecimal.ZERO, null));
    recurring.create(
        new RecurringPlanRequest(
            river.id(),
            "Class booking app, monthly licence",
            new BigDecimal("300.00"),
            new BigDecimal("8"),
            null));

    log.info("Added demo data: 5 customers, 6 invoices, 2 monthly plans (all fake)");
  }

  private CustomerResponse customer(
      String name, String email, String country, String currency, int paymentTermsDays) {
    return customers.create(new CustomerRequest(name, email, country, currency, paymentTermsDays));
  }

  private static InvoiceItemRequest item(String description, String qty, String price) {
    return new InvoiceItemRequest(description, new BigDecimal(qty), new BigDecimal(price));
  }

  private InvoiceResponse invoice(
      CustomerResponse customer,
      LocalDate issueDate,
      String taxRate,
      String discount,
      InvoiceItemRequest... items) {
    return invoices.create(
        new InvoiceRequest(
            customer.id(),
            issueDate,
            null,
            new BigDecimal(taxRate),
            new BigDecimal(discount),
            null,
            List.of(items)));
  }

  /** Marks an invoice as sent on a past date without emailing anyone. */
  private void send(InvoiceResponse response, LocalDate sentOn) {
    Invoice invoice = invoices.find(response.id());
    invoice.setStatus(InvoiceStatus.SENT);
    invoice.setSentAt(sentOn.atStartOfDay(clock.getZone()).toInstant());
  }

  private void pay(InvoiceResponse response, BigDecimal amount, LocalDate paidOn, String method) {
    Invoice invoice = invoices.find(response.id());
    payments.save(new Payment(invoice, amount, paidOn, method, null));
    if (amount.compareTo(response.total()) >= 0) {
      invoice.setStatus(InvoiceStatus.PAID);
    }
  }
}
