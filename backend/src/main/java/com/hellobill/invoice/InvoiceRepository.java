package com.hellobill.invoice;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  List<Invoice> findAllByOrderByIdDesc();

  List<Invoice> findByStatusOrderByIdDesc(InvoiceStatus status);

  List<Invoice> findByCustomerIdOrderByIdDesc(Long customerId);

  List<Invoice> findByStatus(InvoiceStatus status);

  List<Invoice> findByStatusIn(Collection<InvoiceStatus> statuses);
}
