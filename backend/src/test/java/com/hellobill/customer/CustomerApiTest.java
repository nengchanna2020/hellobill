package com.hellobill.customer;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CustomerApiTest extends IntegrationTest {

  @Test
  void createsAndListsCustomersByName() throws Exception {
    createCustomer("Zebra Print Shop", "USD");
    createCustomer("Apple Orchard Co", "EUR");

    mvc.perform(get("/api/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name").value("Apple Orchard Co"))
        .andExpect(jsonPath("$[0].currency").value("EUR"));
  }

  @Test
  void updatesACustomer() throws Exception {
    long id = createCustomer("Lotus Bakery", "USD");

    mvc.perform(
            put("/api/customers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "Lotus Bakery & Cafe", "email": "new@example.com",
                     "country": "KH", "currency": "KHR"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Lotus Bakery & Cafe"))
        .andExpect(jsonPath("$.currency").value("KHR"));
  }

  @Test
  void rejectsBadEmailAndCountry() throws Exception {
    mvc.perform(
            post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "Bad Data", "email": "not-an-email", "country": "Cambodia",
                     "currency": "USD"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.email").exists())
        .andExpect(jsonPath("$.fields.country").exists());
  }

  @Test
  void rejectsUnknownCurrency() throws Exception {
    mvc.perform(
            post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "Bad Data", "email": "a@example.com", "country": "KH",
                     "currency": "XYZ"}
                    """))
        .andExpect(status().isBadRequest());
  }
}
