package com.hellobill.payment;

import com.hellobill.invoice.InvoiceResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/payments")
public class PaymentController {

  private final PaymentService service;

  public PaymentController(PaymentService service) {
    this.service = service;
  }

  @GetMapping
  public List<PaymentResponse> list(@PathVariable Long invoiceId) {
    return service.list(invoiceId);
  }

  /** Records a payment and returns the updated invoice. */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InvoiceResponse record(
      @PathVariable Long invoiceId, @Valid @RequestBody PaymentRequest request) {
    return service.record(invoiceId, request);
  }
}
