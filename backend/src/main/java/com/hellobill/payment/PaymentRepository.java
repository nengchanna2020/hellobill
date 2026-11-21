package com.hellobill.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByInvoiceIdOrderByPaidOnAscIdAsc(Long invoiceId);

  List<Payment> findByInvoiceCustomerIdOrderByPaidOnDescIdDesc(Long customerId);

  @Query("select coalesce(sum(p.amount), 0) from Payment p where p.invoice.id = :invoiceId")
  BigDecimal totalPaid(Long invoiceId);

  List<Payment> findByPaidOnBetween(LocalDate from, LocalDate to);
}
