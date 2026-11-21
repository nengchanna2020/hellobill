package com.hellobill.invoice;

import com.hellobill.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Human-friendly number, for example INV-0001. */
  private String number;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  @Column(length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @Column(name = "issue_date")
  private LocalDate issueDate;

  @Column(name = "payment_terms_days")
  private int paymentTermsDays = 30;

  /** Tax rate in percent, for example 10.00 for 10%. */
  @Column(name = "tax_rate")
  private BigDecimal taxRate = BigDecimal.ZERO;

  /** Discount in percent of the subtotal, for example 15.00 for 15% off. */
  @Column(name = "discount_percent")
  private BigDecimal discountPercent = BigDecimal.ZERO;

  private String notes;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "recurring_plan_id")
  private Long recurringPlanId;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position")
  private List<InvoiceItem> items = new ArrayList<>();

  protected Invoice() {}

  public Invoice(String number, Customer customer, LocalDate issueDate) {
    this.number = number;
    this.customer = customer;
    this.currency = customer.getCurrency();
    this.issueDate = issueDate;
  }

  public void replaceItems(List<InvoiceItem> newItems) {
    items.clear();
    for (int i = 0; i < newItems.size(); i++) {
      InvoiceItem item = newItems.get(i);
      item.attach(this, i + 1);
      items.add(item);
    }
  }

  public Long getId() {
    return id;
  }

  public String getNumber() {
    return number;
  }

  public Customer getCustomer() {
    return customer;
  }

  public String getCurrency() {
    return currency;
  }

  public InvoiceStatus getStatus() {
    return status;
  }

  public void setStatus(InvoiceStatus status) {
    this.status = status;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(LocalDate issueDate) {
    this.issueDate = issueDate;
  }

  public int getPaymentTermsDays() {
    return paymentTermsDays;
  }

  public void setPaymentTermsDays(int paymentTermsDays) {
    this.paymentTermsDays = paymentTermsDays;
  }

  public BigDecimal getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(BigDecimal taxRate) {
    this.taxRate = taxRate;
  }

  public BigDecimal getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public void setSentAt(Instant sentAt) {
    this.sentAt = sentAt;
  }

  public Long getRecurringPlanId() {
    return recurringPlanId;
  }

  public void setRecurringPlanId(Long recurringPlanId) {
    this.recurringPlanId = recurringPlanId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<InvoiceItem> getItems() {
    return items;
  }
}
