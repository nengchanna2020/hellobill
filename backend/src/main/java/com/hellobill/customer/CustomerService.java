package com.hellobill.customer;

import com.hellobill.common.BadRequestException;
import com.hellobill.common.NotFoundException;
import java.util.Currency;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

  private final CustomerRepository customers;

  public CustomerService(CustomerRepository customers) {
    this.customers = customers;
  }

  @Transactional(readOnly = true)
  public List<CustomerResponse> list() {
    return customers.findAllByOrderByNameAsc().stream().map(CustomerResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public CustomerResponse get(Long id) {
    return CustomerResponse.from(find(id));
  }

  @Transactional
  public CustomerResponse create(CustomerRequest request) {
    checkCurrency(request.currency());
    Customer saved =
        customers.save(
            new Customer(
                request.name().trim(),
                request.email().trim(),
                request.country(),
                request.currency()));
    return CustomerResponse.from(saved);
  }

  @Transactional
  public CustomerResponse update(Long id, CustomerRequest request) {
    checkCurrency(request.currency());
    Customer customer = find(id);
    customer.setName(request.name().trim());
    customer.setEmail(request.email().trim());
    customer.setCountry(request.country());
    customer.setCurrency(request.currency());
    return CustomerResponse.from(customer);
  }

  public Customer find(Long id) {
    return customers.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
  }

  private static void checkCurrency(String code) {
    try {
      Currency.getInstance(code);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Unknown currency: " + code);
    }
  }
}
