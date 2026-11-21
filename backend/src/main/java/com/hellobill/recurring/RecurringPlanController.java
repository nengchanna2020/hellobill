package com.hellobill.recurring;

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
@RequestMapping("/api/recurring-plans")
public class RecurringPlanController {

  private final RecurringBillingService service;

  public RecurringPlanController(RecurringBillingService service) {
    this.service = service;
  }

  @GetMapping
  public List<RecurringPlanResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RecurringPlanResponse create(@Valid @RequestBody RecurringPlanRequest request) {
    return service.create(request);
  }

  @PostMapping("/{id}/pause")
  public RecurringPlanResponse pause(@PathVariable Long id) {
    return service.setActive(id, false);
  }

  @PostMapping("/{id}/resume")
  public RecurringPlanResponse resume(@PathVariable Long id) {
    return service.setActive(id, true);
  }

  /** Runs monthly billing now instead of waiting for 06:00. Handy for demos. */
  @PostMapping("/run")
  public List<InvoiceResponse> runNow() {
    return service.runDuePlans();
  }
}
