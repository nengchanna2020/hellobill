package com.hellobill.invoice;

import com.hellobill.common.BadRequestException;
import com.hellobill.common.Money;
import com.hellobill.common.NotFoundException;
import com.hellobill.customer.Customer;
import com.hellobill.customer.CustomerService;
import com.hellobill.mail.InvoiceMailer;
import com.hellobill.payment.PaymentRepository;
import com.hellobill.payment.PaymentResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

  private final InvoiceRepository invoices;
  private final PaymentRepository payments;
  private final CustomerService customers;
  private final InvoiceNumberGenerator numbers;
  private final InvoiceMailer mailer;
  private final Clock clock;

  public InvoiceService(
      InvoiceRepository invoices,
      PaymentRepository payments,
      CustomerService customers,
      InvoiceNumberGenerator numbers,
      InvoiceMailer mailer,
      Clock clock) {
    this.invoices = invoices;
    this.payments = payments;
    this.customers = customers;
    this.numbers = numbers;
    this.mailer = mailer;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public List<InvoiceSummary> list(InvoiceStatus status, Long customerId) {
    List<Invoice> found;
    if (customerId != null) {
      found = invoices.findByCustomerIdOrderByIdDesc(customerId);
    } else if (status != null) {
      found = invoices.findByStatusOrderByIdDesc(status);
    } else {
      found = invoices.findAllByOrderByIdDesc();
    }
    return found.stream()
        .filter(i -> status == null || i.getStatus() == status)
        .map(this::toSummary)
        .toList();
  }

  @Transactional(readOnly = true)
  public InvoiceResponse get(Long id) {
    return toResponse(find(id));
  }

  @Transactional
  public InvoiceResponse create(InvoiceRequest request) {
    Customer customer = customers.find(request.customerId());
    LocalDate issueDate = request.issueDate() != null ? request.issueDate() : LocalDate.now(clock);
    Invoice invoice = new Invoice(numbers.next(), customer, issueDate);
    invoice.setPaymentTermsDays(customer.getDefaultPaymentTermsDays());
    apply(invoice, request);
    return toResponse(invoices.save(invoice));
  }

  @Transactional
  public InvoiceResponse update(Long id, InvoiceRequest request) {
    Invoice invoice = find(id);
    requireDraft(invoice, "edited");
    if (!invoice.getCustomer().getId().equals(request.customerId())) {
      throw new BadRequestException("The customer of an invoice can't be changed");
    }
    if (request.issueDate() != null) {
      invoice.setIssueDate(request.issueDate());
    }
    apply(invoice, request);
    return toResponse(invoice);
  }

  @Transactional
  public void delete(Long id) {
    Invoice invoice = find(id);
    requireDraft(invoice, "deleted");
    invoices.delete(invoice);
  }

  /** Emails the invoice to the customer and marks it as sent. */
  @Transactional
  public InvoiceResponse send(Long id) {
    Invoice invoice = find(id);
    if (invoice.getStatus() == InvoiceStatus.PAID) {
      throw new BadRequestException("Invoice " + invoice.getNumber() + " is already paid");
    }
    InvoiceResponse response = toResponse(invoice);
    mailer.sendInvoice(response);
    if (invoice.getStatus() == InvoiceStatus.DRAFT) {
      invoice.setStatus(InvoiceStatus.SENT);
    }
    invoice.setSentAt(Instant.now(clock));
    return toResponse(invoice);
  }

  public Invoice find(Long id) {
    return invoices.findById(id).orElseThrow(() -> new NotFoundException("Invoice", id));
  }

  public BigDecimal amountPaid(Invoice invoice) {
    if (invoice.getId() == null) {
      return Money.ZERO;
    }
    return Money.round(payments.totalPaid(invoice.getId()));
  }

  public InvoiceResponse toResponse(Invoice invoice) {
    InvoiceTotals totals = InvoiceCalculator.totals(invoice);
    BigDecimal paid = amountPaid(invoice);
    Customer c = invoice.getCustomer();
    List<InvoiceResponse.Item> items =
        invoice.getItems().stream()
            .map(
                item ->
                    new InvoiceResponse.Item(
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        InvoiceCalculator.lineTotal(item)))
            .toList();
    List<PaymentResponse> paymentList =
        invoice.getId() == null
            ? List.of()
            : payments.findByInvoiceIdOrderByPaidOnAscIdAsc(invoice.getId()).stream()
                .map(PaymentResponse::from)
                .toList();
    return new InvoiceResponse(
        invoice.getId(),
        invoice.getNumber(),
        invoice.getStatus(),
        new InvoiceResponse.CustomerSummary(c.getId(), c.getName(), c.getEmail()),
        invoice.getCurrency(),
        invoice.getIssueDate(),
        invoice.getPaymentTermsDays(),
        invoice.getDueDate(),
        invoice.getTaxRate(),
        invoice.getDiscountPercent(),
        invoice.getNotes(),
        invoice.getSentAt(),
        invoice.getRecurringPlanId(),
        items,
        totals.subtotal(),
        totals.discount(),
        totals.tax(),
        totals.total(),
        paid,
        totals.total().subtract(paid),
        paymentList);
  }

  public InvoiceSummary toSummary(Invoice invoice) {
    InvoiceTotals totals = InvoiceCalculator.totals(invoice);
    BigDecimal paid = amountPaid(invoice);
    return new InvoiceSummary(
        invoice.getId(),
        invoice.getNumber(),
        invoice.getStatus(),
        invoice.getCustomer().getId(),
        invoice.getCustomer().getName(),
        invoice.getCurrency(),
        invoice.getIssueDate(),
        invoice.getDueDate(),
        totals.total(),
        totals.total().subtract(paid));
  }

  private static void apply(Invoice invoice, InvoiceRequest request) {
    if (request.paymentTermsDays() != null) {
      invoice.setPaymentTermsDays(request.paymentTermsDays());
    }
    invoice.setTaxRate(request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO);
    invoice.setDiscountPercent(
        request.discountPercent() != null ? request.discountPercent() : BigDecimal.ZERO);
    invoice.setNotes(request.notes());
    invoice.replaceItems(
        request.items().stream()
            .map(i -> new InvoiceItem(i.description().trim(), i.quantity(), i.unitPrice()))
            .toList());
  }

  private static void requireDraft(Invoice invoice, String action) {
    if (invoice.getStatus() != InvoiceStatus.DRAFT) {
      throw new BadRequestException(
          "Only draft invoices can be "
              + action
              + ". "
              + invoice.getNumber()
              + " is "
              + invoice.getStatus().name().toLowerCase()
              + ".");
    }
  }
}
