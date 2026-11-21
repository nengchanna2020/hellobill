package com.hellobill.invoice;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OverdueInvoicesApiTest extends IntegrationTest {

  private static final String ONE_ITEM =
      "[{\"description\": \"Consulting\", \"quantity\": 1, \"unitPrice\": 100.00}]";

  private long invoiceIssued(long customerId, LocalDate issueDate) throws Exception {
    String body =
        postJson(
            "/api/invoices",
            """
            {"customerId": %d, "issueDate": "%s", "paymentTermsDays": 30, "items": %s}
            """
                .formatted(customerId, issueDate, ONE_ITEM));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  private void pay(long invoiceId, String amount) throws Exception {
    mvc.perform(
            post("/api/invoices/{id}/payments", invoiceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": %s, \"method\": \"BANK_TRANSFER\"}".formatted(amount)))
        .andExpect(status().isCreated());
  }

  @Test
  void listsOverdueInvoicesWithDaysLateMostLateFirst() throws Exception {
    LocalDate today = LocalDate.now();
    long customer = createCustomer("Riverbend", "USD");
    long recent = invoiceIssued(customer, today.minusDays(37)); // due 7 days ago
    long oldest = invoiceIssued(customer, today.minusDays(80)); // due 50 days ago
    send(recent);
    send(oldest);
    pay(recent, "40.00");

    mvc.perform(get("/api/invoices/overdue"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(oldest))
        .andExpect(jsonPath("$[0].daysLate").value(50))
        .andExpect(jsonPath("$[0].balanceDue").value(100.00))
        .andExpect(jsonPath("$[0].dueDate").value(today.minusDays(50).toString()))
        .andExpect(jsonPath("$[1].id").value(recent))
        .andExpect(jsonPath("$[1].daysLate").value(7))
        .andExpect(jsonPath("$[1].balanceDue").value(60.00));
  }

  @Test
  void leavesOutDraftsPaidAndNotYetDueInvoices() throws Exception {
    LocalDate today = LocalDate.now();
    long customer = createCustomer("Riverbend", "USD");
    invoiceIssued(customer, today.minusDays(80)); // old but still a draft
    long paid = invoiceIssued(customer, today.minusDays(80));
    send(paid);
    pay(paid, "100.00");
    send(invoiceIssued(customer, today.minusDays(5))); // due in 25 days

    mvc.perform(get("/api/invoices/overdue"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void invoiceDueTodayIsZeroDaysLate() throws Exception {
    long id = invoiceIssued(createCustomer("Riverbend", "USD"), LocalDate.now().minusDays(30));
    send(id);

    mvc.perform(get("/api/invoices/overdue"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(id))
        .andExpect(jsonPath("$[0].daysLate").value(0));
  }
}
