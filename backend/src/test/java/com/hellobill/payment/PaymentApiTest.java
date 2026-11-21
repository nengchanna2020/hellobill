package com.hellobill.payment;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class PaymentApiTest extends IntegrationTest {

  private static final String ONE_ITEM =
      "[{\"description\": \"Photo shoot\", \"quantity\": 1, \"unitPrice\": 300.00}]";

  private ResultActions pay(long invoiceId, String amount) throws Exception {
    return mvc.perform(
        post("/api/invoices/{id}/payments", invoiceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"amount\": %s, \"method\": \"BANK_TRANSFER\"}".formatted(amount)));
  }

  @Test
  void partialPaymentLowersTheBalance() throws Exception {
    long id = createInvoice(createCustomer("Maple Studio", "CAD"), ONE_ITEM);
    send(id);

    pay(id, "100.00")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("SENT"))
        .andExpect(jsonPath("$.amountPaid").value(100.00))
        .andExpect(jsonPath("$.balanceDue").value(200.00));
  }

  @Test
  void payingTheRestMarksItPaid() throws Exception {
    long id = createInvoice(createCustomer("Maple Studio", "CAD"), ONE_ITEM);
    send(id);

    pay(id, "100.00");
    pay(id, "200.00")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PAID"))
        .andExpect(jsonPath("$.balanceDue").value(0));

    mvc.perform(get("/api/invoices/{id}/payments", id)).andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void cannotPayMoreThanTheBalance() throws Exception {
    long id = createInvoice(createCustomer("Maple Studio", "CAD"), ONE_ITEM);
    send(id);

    pay(id, "300.01").andExpect(status().isBadRequest());
  }

  @Test
  void cannotPayADraft() throws Exception {
    long id = createInvoice(createCustomer("Maple Studio", "CAD"), ONE_ITEM);

    pay(id, "50.00").andExpect(status().isBadRequest());
  }

  @Test
  void rejectsZeroPayment() throws Exception {
    long id = createInvoice(createCustomer("Maple Studio", "CAD"), ONE_ITEM);
    send(id);

    pay(id, "0").andExpect(status().isBadRequest());
  }
}
