package com.hellobill.evalcheck;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;

class CustomerSearchCheck extends IntegrationTest {

  @Test
  void searchesByNameIgnoringCase() throws Exception {
    createCustomer("Riverbend Fitness", "USD");
    createCustomer("River Yoga", "USD");
    createCustomer("Lotus Bakery", "USD");

    mvc.perform(get("/api/customers").param("search", "river"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].name", containsInAnyOrder("Riverbend Fitness", "River Yoga")));
    mvc.perform(get("/api/customers").param("search", "RIVER"))
        .andExpect(jsonPath("$", hasSize(2)));
    mvc.perform(get("/api/customers").param("search", "bake"))
        .andExpect(jsonPath("$[*].name", containsInAnyOrder("Lotus Bakery")));
    mvc.perform(get("/api/customers").param("search", "zzz")).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void withoutSearchListsEveryone() throws Exception {
    createCustomer("Riverbend Fitness", "USD");
    createCustomer("Lotus Bakery", "USD");

    mvc.perform(get("/api/customers")).andExpect(jsonPath("$", hasSize(2)));
  }
}
