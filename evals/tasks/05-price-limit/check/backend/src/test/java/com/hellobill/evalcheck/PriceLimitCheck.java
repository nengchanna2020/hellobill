package com.hellobill.evalcheck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PriceLimitCheck extends IntegrationTest {

  private String invoice(long customerId, String unitPrice) {
    return """
        {"customerId": %d, "taxRate": 0, "discountPercent": 0,
         "items": [{"description": "Big job", "quantity": 1, "unitPrice": %s}]}
        """
        .formatted(customerId, unitPrice);
  }

  @Test
  void rejectsAUnitPriceAboveOneMillion() throws Exception {
    long id = createCustomer("Riverbend Fitness", "USD");
    mvc.perform(
            post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invoice(id, "1000000.01")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void acceptsExactlyOneMillion() throws Exception {
    long id = createCustomer("Riverbend Fitness", "USD");
    mvc.perform(
            post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invoice(id, "1000000.00")))
        .andExpect(status().isCreated());
  }
}
