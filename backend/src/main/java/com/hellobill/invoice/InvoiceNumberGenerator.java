package com.hellobill.invoice;

import org.springframework.stereotype.Component;

/** Gives each new invoice the next number: INV-0001, INV-0002, ... */
@Component
public class InvoiceNumberGenerator {

  private final InvoiceRepository invoices;

  public InvoiceNumberGenerator(InvoiceRepository invoices) {
    this.invoices = invoices;
  }

  public String next() {
    long next = invoices.count() + 1;
    return "INV-%04d".formatted(next);
  }
}
