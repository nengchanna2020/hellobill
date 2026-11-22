package com.hellobill.invoice;

import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Marks sent invoices as overdue once their payment terms have passed. */
@Component
public class OverdueJob {

  private static final Logger log = LoggerFactory.getLogger(OverdueJob.class);

  private final InvoiceRepository invoices;
  private final Clock clock;

  public OverdueJob(InvoiceRepository invoices, Clock clock) {
    this.invoices = invoices;
    this.clock = clock;
  }

  /** Every night at 01:00, and once when the app starts. */
  @Scheduled(cron = "0 0 1 * * *")
  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public int markOverdue() {
    LocalDate today = LocalDate.now(clock);
    int count = 0;
    for (Invoice invoice : invoices.findByStatus(InvoiceStatus.SENT)) {
      LocalDate dueDate = invoice.getDueDate();
      if (dueDate.isBefore(today)) {
        invoice.setStatus(InvoiceStatus.OVERDUE);
        count++;
      }
    }
    if (count > 0) {
      log.info("Marked {} invoice(s) as overdue", count);
    }
    return count;
  }
}
