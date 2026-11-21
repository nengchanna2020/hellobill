package com.hellobill.customer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
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
  void defaultPaymentTermsAre30UnlessGiven() throws Exception {
    long plain = createCustomer("Plain Co", "USD");
    long net45 = createCustomer("Net45 Co", "USD", 45);

    mvc.perform(get("/api/customers/{id}", plain))
        .andExpect(jsonPath("$.defaultPaymentTermsDays").value(30));
    mvc.perform(get("/api/customers/{id}", net45))
        .andExpect(jsonPath("$.defaultPaymentTermsDays").value(45));
  }

  @Test
  void updatesAndKeepsDefaultPaymentTerms() throws Exception {
    long id = createCustomer("Lotus Bakery", "USD", 45);

    // Leaving the field out keeps the current value.
    mvc.perform(
            put("/api/customers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "Lotus Bakery", "email": "a@example.com",
                     "country": "KH", "currency": "USD"}
                    """))
        .andExpect(jsonPath("$.defaultPaymentTermsDays").value(45));

    mvc.perform(
            put("/api/customers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "Lotus Bakery", "email": "a@example.com",
                     "country": "KH", "currency": "USD", "defaultPaymentTermsDays": 7}
                    """))
        .andExpect(jsonPath("$.defaultPaymentTermsDays").value(7));
  }

  @Test
  void rejectsOutOfRangePaymentTerms() throws Exception {
    for (int days : new int[] {-1, 366}) {
      mvc.perform(
              post("/api/customers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"name": "Bad Terms", "email": "a@example.com", "country": "KH",
                       "currency": "USD", "defaultPaymentTermsDays": %d}
                      """
                          .formatted(days)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.fields.defaultPaymentTermsDays").exists());
    }
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

  @Test
  void storesTrimsAndClearsPhone() throws Exception {
    String body =
        postJson(
            "/api/customers",
            """
            {"name": "Phone Co", "email": "a@example.com", "phone": " +855 12 345 678 ",
             "country": "KH", "currency": "USD"}
            """);
    long id = ((Number) JsonPath.read(body, "$.id")).longValue();

    mvc.perform(get("/api/customers/{id}", id))
        .andExpect(jsonPath("$.phone").value("+855 12 345 678"));

    // An update with a blank phone clears it.
    mvc.perform(
            put("/api/customers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "Phone Co", "email": "a@example.com", "phone": "",
                     "country": "KH", "currency": "USD"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phone").value(nullValue()));

    long plain = createCustomer("No Phone Co", "USD");
    mvc.perform(get("/api/customers/{id}", plain))
        .andExpect(jsonPath("$.phone").value(nullValue()));
  }

  @Test
  void rejectsBadPhone() throws Exception {
    for (String phone : new String[] {"call me", "12", "+1".repeat(20)}) {
      mvc.perform(
              post("/api/customers")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"name": "Bad Phone", "email": "a@example.com", "phone": "%s",
                       "country": "KH", "currency": "USD"}
                      """
                          .formatted(phone)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.fields.phone").exists());
    }
  }
}
