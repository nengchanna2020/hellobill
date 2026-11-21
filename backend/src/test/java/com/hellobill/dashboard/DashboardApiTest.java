package com.hellobill.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class DashboardApiTest extends IntegrationTest {

  private static final String ONE_ITEM =
      "[{\"description\": \"Consulting\", \"quantity\": 1, \"unitPrice\": 100.00}]";

  @Test
  void groupsMoneyByCurrency() throws Exception {
    long usd = createCustomer("Riverbend", "USD");
    long eur = createCustomer("Kranich", "EUR");
    long a = createInvoice(usd, ONE_ITEM);
    long b = createInvoice(usd, ONE_ITEM);
    long c = createInvoice(eur, ONE_ITEM);
    createInvoice(eur, ONE_ITEM); // draft, not counted as outstanding
    send(a);
    send(b);
    send(c);
    mvc.perform(
        post("/api/invoices/{id}/payments", a)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"amount\": 100.00, \"method\": \"CASH\"}"));

    mvc.perform(get("/api/dashboard"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.counts.DRAFT").value(1))
        .andExpect(jsonPath("$.counts.SENT").value(2))
        .andExpect(jsonPath("$.counts.PAID").value(1))
        .andExpect(jsonPath("$.outstanding[0].currency").value("EUR"))
        .andExpect(jsonPath("$.outstanding[0].amount").value(100.00))
        .andExpect(jsonPath("$.outstanding[1].currency").value("USD"))
        .andExpect(jsonPath("$.outstanding[1].amount").value(100.00))
        .andExpect(jsonPath("$.paidThisMonth[0].amount").value(100.00))
        .andExpect(jsonPath("$.recentInvoices.length()").value(4));
  }
}
