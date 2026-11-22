package com.hellobill.recurring;

import com.hellobill.customer.Customer;
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

/** Bills a customer the same amount every month, for example a support plan. */
@Entity
@Table(name = "recurring_plans")
public class RecurringPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  private String description;

  private BigDecimal amount;

  @Column(name = "tax_rate")
  private BigDecimal taxRate;

  private boolean active = true;

  /** When true, invoices made by this plan are emailed straight away. */
  @Column(name = "auto_send")
  private boolean autoSend;

  /** The next day this plan creates an invoice. */
  @Column(name = "next_run_date")
  private LocalDate nextRunDate;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt = Instant.now();

  protected RecurringPlan() {}

  public RecurringPlan(
      Customer customer,
      String description,
      BigDecimal amount,
      BigDecimal taxRate,
      LocalDate nextRunDate,
      boolean autoSend) {
    this.customer = customer;
    this.description = description;
    this.amount = amount;
    this.taxRate = taxRate;
    this.nextRunDate = nextRunDate;
    this.autoSend = autoSend;
  }

  public boolean isAutoSend() {
    return autoSend;
  }

  public void setAutoSend(boolean autoSend) {
    this.autoSend = autoSend;
  }

  public Long getId() {
    return id;
  }

  public Customer getCustomer() {
    return customer;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getTaxRate() {
    return taxRate;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public LocalDate getNextRunDate() {
    return nextRunDate;
  }

  public void setNextRunDate(LocalDate nextRunDate) {
    this.nextRunDate = nextRunDate;
  }
}
