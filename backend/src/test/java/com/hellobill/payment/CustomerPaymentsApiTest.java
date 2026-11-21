package com.hellobill.payment;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CustomerPaymentsApiTest extends IntegrationTest {

  private static final String ONE_ITEM =
      "[{\"description\": \"Photo shoot\", \"quantity\": 1, \"unitPrice\": 300.00}]";

  private void pay(long invoiceId, String amount, String paidOn) throws Exception {
    mvc.perform(
            post("/api/invoices/{id}/payments", invoiceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"amount\": %s, \"method\": \"CARD\", \"paidOn\": \"%s\"}"
                        .formatted(amount, paidOn)))
        .andExpect(status().isCreated());
  }

  @Test
  void listsPaymentsAcrossInvoicesNewestFirstAndOnlyForThatCustomer() throws Exception {
    long maple = createCustomer("Maple Studio", "CAD");
    long other = createCustomer("Other Co", "CAD");
    long first = createInvoice(maple, ONE_ITEM);
    long second = createInvoice(maple, ONE_ITEM);
    long othersInvoice = createInvoice(other, ONE_ITEM);
    send(first);
    send(second);
    send(othersInvoice);

    pay(first, "10.00", "2026-01-05");
    pay(second, "20.00", "2026-03-10");
    pay(first, "30.00", "2026-02-01");
    pay(othersInvoice, "99.00", "2026-04-01");

    mvc.perform(get("/api/customers/{id}/payments", maple))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].amount").value(20.00))
        .andExpect(jsonPath("$[0].paidOn").value("2026-03-10"))
        .andExpect(jsonPath("$[1].amount").value(30.00))
        .andExpect(jsonPath("$[2].amount").value(10.00));
  }

  @Test
  void customerWithoutPaymentsGetsAnEmptyList() throws Exception {
    long id = createCustomer("Maple Studio", "CAD");

    mvc.perform(get("/api/customers/{id}/payments", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void unknownCustomerIs404() throws Exception {
    mvc.perform(get("/api/customers/{id}/payments", 999)).andExpect(status().isNotFound());
  }

  @Test
  void nonNumericCustomerIdIs400() throws Exception {
    mvc.perform(get("/api/customers/{id}/payments", "abc")).andExpect(status().isBadRequest());
  }
}
