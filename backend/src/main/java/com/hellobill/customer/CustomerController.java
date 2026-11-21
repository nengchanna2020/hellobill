package com.hellobill.customer;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

  private final CustomerService service;

  public CustomerController(CustomerService service) {
    this.service = service;
  }

  @GetMapping
  public List<CustomerResponse> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public CustomerResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public CustomerResponse update(
      @PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
    return service.update(id, request);
  }
}
