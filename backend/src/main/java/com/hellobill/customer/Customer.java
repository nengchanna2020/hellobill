package com.hellobill.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String email;

  /** ISO 3166 two-letter country code, for example "KH" or "DE". */
  @Column(length = 2)
  private String country;

  /** ISO 4217 currency code the customer is billed in, for example "USD". */
  @Column(length = 3)
  private String currency;

  /** Payment terms in days that new invoices for this customer start with. */
  @Column(name = "default_payment_terms_days")
  private int defaultPaymentTermsDays = 30;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  protected Customer() {}

  public Customer(String name, String email, String country, String currency) {
    this.name = name;
    this.email = email;
    this.country = country;
    this.currency = currency;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getDefaultPaymentTermsDays() {
    return defaultPaymentTermsDays;
  }

  public void setDefaultPaymentTermsDays(int defaultPaymentTermsDays) {
    this.defaultPaymentTermsDays = defaultPaymentTermsDays;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
