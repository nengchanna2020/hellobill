package com.hellobill.recurring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RecurringBillingTest extends IntegrationTest {

  private long createPlan(long customerId, LocalDate start) throws Exception {
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Support plan", "amount": 250.00,
             "taxRate": 10, "startDate": "%s"}
            """
                .formatted(customerId, start));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  @Test
  void newPlanStartsOnTheFirstOfNextMonth() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Support plan", "amount": 250.00}
            """
                .formatted(customer));

    LocalDate expected = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    assertThat((String) JsonPath.read(body, "$.nextRunDate")).isEqualTo(expected.toString());
  }

  @Test
  void duePlanCreatesADraftAndMovesOnAMonth() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    LocalDate start = LocalDate.now().minusDays(1);
    createPlan(customer, start);

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("DRAFT"))
        .andExpect(jsonPath("$[0].total").value(275.00));

    mvc.perform(get("/api/recurring-plans"))
        .andExpect(jsonPath("$[0].nextRunDate").value(start.plusMonths(1).toString()));

    // Running again the same day makes nothing new.
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void pausedPlansDoNotBill() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long plan = createPlan(customer, LocalDate.now());

    mvc.perform(post("/api/recurring-plans/{id}/pause", plan))
        .andExpect(jsonPath("$.active").value(false));
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));
  }
}
