package com.hellobill.payment;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/payments")
public class CustomerPaymentController {

  private final PaymentService service;

  public CustomerPaymentController(PaymentService service) {
    this.service = service;
  }

  /** All payments on a customer's invoices, newest first. */
  @GetMapping
  public List<PaymentResponse> list(@PathVariable Long customerId) {
    return service.listForCustomer(customerId);
  }
}
