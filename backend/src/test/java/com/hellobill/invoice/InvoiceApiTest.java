package com.hellobill.invoice;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;

class InvoiceApiTest extends IntegrationTest {

  private static final String TWO_ITEMS =
      """
      [{"description": "Design work", "quantity": 3, "unitPrice": 40.00},
       {"description": "Hosting", "quantity": 1, "unitPrice": 15.50}]
      """;

  @Test
  void createsInvoiceWithNumberAndTotals() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long id = createInvoice(customer, TWO_ITEMS, "10", "0");

    mvc.perform(get("/api/invoices/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.number").value("INV-0001"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].lineTotal").value(120.00))
        .andExpect(jsonPath("$.subtotal").value(135.50))
        .andExpect(jsonPath("$.tax").value(13.55))
        .andExpect(jsonPath("$.total").value(149.05))
        .andExpect(jsonPath("$.balanceDue").value(149.05));
  }

  @Test
  void dueDateIsIssueDatePlusPaymentTerms() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    String created =
        postJson(
            "/api/invoices",
            """
            {"customerId": %d, "issueDate": "2026-01-20", "paymentTermsDays": 14,
             "items": %s}
            """
                .formatted(customer, TWO_ITEMS));
    long id = ((Number) JsonPath.read(created, "$.id")).longValue();

    mvc.perform(get("/api/invoices/{id}", id)).andExpect(jsonPath("$.dueDate").value("2026-02-03"));
    mvc.perform(get("/api/invoices")).andExpect(jsonPath("$[0].dueDate").value("2026-02-03"));

    mvc.perform(
            put("/api/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"customerId\": %d, \"paymentTermsDays\": 30, \"items\": %s}"
                        .formatted(customer, TWO_ITEMS)))
        .andExpect(jsonPath("$.dueDate").value("2026-02-19"));
  }

  @Test
  void numbersInvoicesInOrder() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    createInvoice(customer, TWO_ITEMS);
    createInvoice(customer, TWO_ITEMS);
    long third = createInvoice(customer, TWO_ITEMS);

    mvc.perform(get("/api/invoices/{id}", third)).andExpect(jsonPath("$.number").value("INV-0003"));
  }

  @Test
  void invoiceUsesTheCustomersCurrency() throws Exception {
    long customer = createCustomer("Kranich Design", "EUR");
    long id = createInvoice(customer, TWO_ITEMS);

    mvc.perform(get("/api/invoices/{id}", id)).andExpect(jsonPath("$.currency").value("EUR"));
  }

  @Test
  void rejectsInvoiceWithoutItems() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");

    mvc.perform(
            post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\": %d, \"items\": []}".formatted(customer)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.items").exists());
  }

  @Test
  void sendingEmailsTheCustomerAndMarksItSent() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long id = createInvoice(customer, TWO_ITEMS);

    mvc.perform(post("/api/invoices/{id}/send", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SENT"))
        .andExpect(jsonPath("$.sentAt").exists());

    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void draftsCanBeEdited() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long id = createInvoice(customer, TWO_ITEMS);

    mvc.perform(
            put("/api/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"customerId": %d, "paymentTermsDays": 14,
                     "items": [{"description": "Consulting", "quantity": 2, "unitPrice": 100}]}
                    """
                        .formatted(customer)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentTermsDays").value(14))
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.total").value(200.00));
  }

  @Test
  void sentInvoicesCannotBeEditedOrDeleted() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long id = createInvoice(customer, TWO_ITEMS);
    send(id);

    mvc.perform(
            put("/api/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\": %d, \"items\": %s}".formatted(customer, TWO_ITEMS)))
        .andExpect(status().isBadRequest());
    mvc.perform(delete("/api/invoices/{id}", id)).andExpect(status().isBadRequest());
  }

  @Test
  void draftsCanBeDeleted() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long id = createInvoice(customer, TWO_ITEMS);

    mvc.perform(delete("/api/invoices/{id}", id)).andExpect(status().isNoContent());
    mvc.perform(get("/api/invoices/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void listsInvoicesByStatus() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long sent = createInvoice(customer, TWO_ITEMS);
    createInvoice(customer, TWO_ITEMS);
    send(sent);

    mvc.perform(get("/api/invoices").param("status", "SENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(sent));
    mvc.perform(get("/api/invoices")).andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void unknownInvoiceIs404() throws Exception {
    mvc.perform(get("/api/invoices/999")).andExpect(status().isNotFound());
  }
}
