package com.hellobill.invoice;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  private final InvoiceService service;

  public InvoiceController(InvoiceService service) {
    this.service = service;
  }

  @GetMapping
  public List<InvoiceSummary> list(
      @RequestParam(required = false) InvoiceStatus status,
      @RequestParam(required = false) Long customerId) {
    return service.list(status, customerId);
  }

  @GetMapping("/{id}")
  public InvoiceResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InvoiceResponse create(@Valid @RequestBody InvoiceRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public InvoiceResponse update(@PathVariable Long id, @Valid @RequestBody InvoiceRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @PostMapping("/{id}/send")
  public InvoiceResponse send(@PathVariable Long id) {
    return service.send(id);
  }
}
