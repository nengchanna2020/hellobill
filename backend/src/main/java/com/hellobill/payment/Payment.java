package com.hellobill.payment;

import com.hellobill.invoice.Invoice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "invoice_id")
  private Invoice invoice;

  private BigDecimal amount;

  @Column(name = "paid_on")
  private LocalDate paidOn;

  /** How the money came in, for example BANK_TRANSFER, CARD or CASH. */
  private String method;

  private String note;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt = Instant.now();

  protected Payment() {}

  public Payment(Invoice invoice, BigDecimal amount, LocalDate paidOn, String method, String note) {
    this.invoice = invoice;
    this.amount = amount;
    this.paidOn = paidOn;
    this.method = method;
    this.note = note;
  }

  public Long getId() {
    return id;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public LocalDate getPaidOn() {
    return paidOn;
  }

  public String getMethod() {
    return method;
  }

  public String getNote() {
    return note;
  }
}
