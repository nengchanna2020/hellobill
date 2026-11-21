package com.hellobill.customer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  List<Customer> findAllByOrderByNameAsc();
}
