package com.hellobill.thing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// The whole app on a real Postgres. Empty tables before each test. Check real values, not just "200".
class ThingApiTest extends IntegrationTest {

  @Test
  void createsAndReadsAThing() throws Exception {
    long customerId = createCustomer("Maple Studio", "CAD"); // use the helpers, not hand-written JSON
    mvc.perform(
            post("/api/things")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Lamp\", \"customerId\": %d}".formatted(customerId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Lamp"));
  }

  @Test
  void unknownThingIs404() throws Exception {
    mvc.perform(get("/api/things/{id}", 999)).andExpect(status().isNotFound());
  }

  @Test
  void blankNameIs400WithTheField() throws Exception {
    mvc.perform(
            post("/api/things").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.name").exists());
  }
}
