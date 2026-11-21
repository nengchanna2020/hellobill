package com.hellobill;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for API tests: the whole app on a real Postgres (Testcontainers), with a mock mail
 * sender so no email leaves the test. Every test starts with empty tables.
 */
@SpringBootTest(properties = "hellobill.demo-data=false")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

  @Autowired protected MockMvc mvc;

  @Autowired protected JdbcTemplate jdbc;

  @MockitoBean protected JavaMailSender mailSender;

  @BeforeEach
  void cleanDatabase() {
    jdbc.execute(
        "TRUNCATE payments, invoice_items, invoices, recurring_plans, customers RESTART IDENTITY"
            + " CASCADE");
  }

  protected String postJson(String url, String json) throws Exception {
    return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  protected long createCustomer(String name, String currency) throws Exception {
    String body =
        postJson(
            "/api/customers",
            """
            {"name": "%s", "email": "test@example.com", "country": "KH", "currency": "%s"}
            """
                .formatted(name, currency));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  protected long createInvoice(long customerId, String itemsJson) throws Exception {
    return createInvoice(customerId, itemsJson, "0", "0");
  }

  protected long createInvoice(long customerId, String itemsJson, String taxRate, String discount)
      throws Exception {
    String body =
        postJson(
            "/api/invoices",
            """
            {"customerId": %d, "taxRate": %s, "discountPercent": %s, "items": %s}
            """
                .formatted(customerId, taxRate, discount, itemsJson));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  protected void send(long invoiceId) throws Exception {
    mvc.perform(post("/api/invoices/{id}/send", invoiceId)).andExpect(status().isOk());
  }
}
