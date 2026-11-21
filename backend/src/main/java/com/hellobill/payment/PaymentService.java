package com.hellobill.payment;

import com.hellobill.common.BadRequestException;
import com.hellobill.customer.CustomerService;
import com.hellobill.invoice.Invoice;
import com.hellobill.invoice.InvoiceCalculator;
import com.hellobill.invoice.InvoiceResponse;
import com.hellobill.invoice.InvoiceService;
import com.hellobill.invoice.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  private final PaymentRepository payments;
  private final InvoiceService invoices;
  private final CustomerService customers;
  private final Clock clock;

  public PaymentService(
      PaymentRepository payments, InvoiceService invoices, CustomerService customers, Clock clock) {
    this.payments = payments;
    this.invoices = invoices;
    this.customers = customers;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public List<PaymentResponse> list(Long invoiceId) {
    invoices.find(invoiceId);
    return payments.findByInvoiceIdOrderByPaidOnAscIdAsc(invoiceId).stream()
        .map(PaymentResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PaymentResponse> listForCustomer(Long customerId) {
    customers.find(customerId);
    return payments.findByInvoiceCustomerIdOrderByPaidOnDescIdDesc(customerId).stream()
        .map(PaymentResponse::from)
        .toList();
  }

  /**
   * Records a full or partial payment. When the invoice is paid in full its status becomes PAID.
   */
  @Transactional
  public InvoiceResponse record(Long invoiceId, PaymentRequest request) {
    Invoice invoice = invoices.find(invoiceId);
    if (invoice.getStatus() == InvoiceStatus.DRAFT) {
      throw new BadRequestException("Send invoice " + invoice.getNumber() + " before paying it");
    }
    if (invoice.getStatus() == InvoiceStatus.PAID) {
      throw new BadRequestException("Invoice " + invoice.getNumber() + " is already paid");
    }

    BigDecimal total = InvoiceCalculator.totals(invoice).total();
    BigDecimal balance = total.subtract(invoices.amountPaid(invoice));
    if (request.amount().compareTo(balance) > 0) {
      throw new BadRequestException(
          "Payment of " + request.amount() + " is more than the balance due of " + balance);
    }

    LocalDate paidOn = request.paidOn() != null ? request.paidOn() : LocalDate.now(clock);
    payments.save(
        new Payment(invoice, request.amount(), paidOn, request.method().trim(), request.note()));

    if (request.amount().compareTo(balance) == 0) {
      invoice.setStatus(InvoiceStatus.PAID);
    }
    return invoices.toResponse(invoice);
  }
}
