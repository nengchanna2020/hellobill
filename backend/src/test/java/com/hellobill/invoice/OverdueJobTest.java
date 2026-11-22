package com.hellobill.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.hellobill.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OverdueJobTest extends IntegrationTest {

  private static final String ONE_ITEM =
      "[{\"description\": \"Consulting\", \"quantity\": 1, \"unitPrice\": 100.00}]";

  @Autowired OverdueJob job;

  private long sentInvoiceIssued(LocalDate issueDate) throws Exception {
    return sentInvoiceIssued(issueDate, 30);
  }

  private long sentInvoiceIssued(LocalDate issueDate, int paymentTermsDays) throws Exception {
    long id =
        Long.parseLong(
            com.jayway.jsonpath.JsonPath.read(
                    postJson(
                        "/api/invoices",
                        """
                        {"customerId": %d, "issueDate": "%s", "paymentTermsDays": %d,
                         "items": %s}
                        """
                            .formatted(
                                createCustomer("Riverbend", "USD"),
                                issueDate,
                                paymentTermsDays,
                                ONE_ITEM)),
                    "$.id")
                .toString());
    send(id);
    return id;
  }

  @Test
  void marksLongUnpaidInvoicesOverdue() throws Exception {
    long id = sentInvoiceIssued(LocalDate.now().minusDays(45));

    assertThat(job.markOverdue()).isEqualTo(1);
    mvc.perform(get("/api/invoices/{id}", id)).andExpect(jsonPath("$.status").value("OVERDUE"));
  }

  @Test
  void leavesRecentInvoicesAlone() throws Exception {
    long id = sentInvoiceIssued(LocalDate.now().minusDays(5));

    assertThat(job.markOverdue()).isZero();
    mvc.perform(get("/api/invoices/{id}", id)).andExpect(jsonPath("$.status").value("SENT"));
  }

  @Test
  void invoiceDueTodayIsNotOverdueUntilTheDayAfter() throws Exception {
    int terms = 14;
    long dueToday = sentInvoiceIssued(LocalDate.now().minusDays(terms), terms);

    assertThat(job.markOverdue()).isZero();
    mvc.perform(get("/api/invoices/{id}", dueToday))
        .andExpect(jsonPath("$.dueDate").value(LocalDate.now().toString()))
        .andExpect(jsonPath("$.status").value("SENT"));

    long dueYesterday = sentInvoiceIssued(LocalDate.now().minusDays(terms + 1), terms);

    assertThat(job.markOverdue()).isEqualTo(1);
    mvc.perform(get("/api/invoices/{id}", dueYesterday))
        .andExpect(jsonPath("$.status").value("OVERDUE"));
  }
}
