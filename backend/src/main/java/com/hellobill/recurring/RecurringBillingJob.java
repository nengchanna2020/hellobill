package com.hellobill.recurring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Runs monthly billing every morning at 06:00. Plans only bill on their next run date. */
@Component
public class RecurringBillingJob {

  private static final Logger log = LoggerFactory.getLogger(RecurringBillingJob.class);

  private final RecurringBillingService service;

  public RecurringBillingJob(RecurringBillingService service) {
    this.service = service;
  }

  @Scheduled(cron = "0 0 6 * * *")
  public void run() {
    int count = service.runDuePlans().size();
    if (count > 0) {
      log.info("Monthly billing created {} draft invoice(s)", count);
    }
  }
}
