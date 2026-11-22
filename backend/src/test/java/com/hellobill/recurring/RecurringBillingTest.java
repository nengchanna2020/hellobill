package com.hellobill.recurring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hellobill.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;

class RecurringBillingTest extends IntegrationTest {

  /** The app's clock in these tests: the real time until a test pins a day with {@link #pin}. */
  static class TestClock extends Clock {
    private volatile Clock delegate = Clock.systemDefaultZone();

    void pin(LocalDate day) {
      delegate = Clock.fixed(day.atTime(6, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    void reset() {
      delegate = Clock.systemDefaultZone();
    }

    @Override
    public ZoneId getZone() {
      return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return delegate.withZone(zone);
    }

    @Override
    public Instant instant() {
      return delegate.instant();
    }
  }

  @TestConfiguration
  static class ClockOverride {
    @Bean
    @Primary
    TestClock testClock() {
      return new TestClock();
    }
  }

  @Autowired TestClock clock;

  @Autowired RecurringBillingJob job;

  @BeforeEach
  void realClock() {
    clock.reset();
  }

  private static final LocalDate DEC_1 = LocalDate.of(2026, 12, 1);

  private long createHostingPlan(long customerId, boolean autoSend) throws Exception {
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Hosting", "amount": 49.00, "taxRate": 0.00,
             "startDate": "2026-12-01", "autoSend": %s}
            """
                .formatted(customerId, autoSend));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  private long createCustomerWithEmail(String name, String email) throws Exception {
    String body =
        postJson(
            "/api/customers",
            """
            {"name": "%s", "email": "%s", "country": "KH", "currency": "USD"}
            """
                .formatted(name, email));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  private void setAutoSend(long plan, String json, boolean expected) throws Exception {
    mvc.perform(
            post("/api/recurring-plans/{id}/auto-send", plan)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.autoSend").value(expected));
  }

  @Test
  void autoSendPlanEmailsTheInvoiceAndMarksItSent() throws Exception {
    clock.pin(DEC_1);
    long customer = createCustomer("Lotus Bakery", "USD");
    createHostingPlan(customer, true);

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].total").value(49.00))
        .andExpect(jsonPath("$[0].status").value("SENT"))
        .andExpect(jsonPath("$[0].sentAt").isNotEmpty())
        .andExpect(jsonPath("$[0].issueDate").value("2026-12-01"));

    ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender, times(1)).send(mail.capture());
    assertThat(mail.getValue().getTo()).containsExactly("test@example.com");
    assertThat(mail.getValue().getSubject()).contains("INV-");
    assertThat(mail.getValue().getText()).contains("Hosting").contains("Total: 49.00 USD");
  }

  @Test
  void planWithoutAutoSendStaysADraftAndSendsNothing() throws Exception {
    clock.pin(DEC_1);
    createHostingPlan(createCustomer("Lotus Bakery", "USD"), false);

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("DRAFT"))
        .andExpect(jsonPath("$[0].sentAt").doesNotExist());

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void autoSendDefaultsToFalseAndIsFalseForExistingRows() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Support plan", "amount": 250.00}
            """
                .formatted(customer));
    assertThat((Boolean) JsonPath.read(body, "$.autoSend")).isFalse();

    // A row inserted without the column, as plans from before V7 are, gets FALSE.
    jdbc.update(
        "INSERT INTO recurring_plans (customer_id, description, amount, tax_rate, active,"
            + " next_run_date) VALUES (?, 'Old plan', 10.00, 0, TRUE, '2027-01-01')",
        customer);
    mvc.perform(get("/api/recurring-plans"))
        .andExpect(jsonPath("$[1].description").value("Old plan"))
        .andExpect(jsonPath("$[1].autoSend").value(false));
  }

  @Test
  void autoSendEndpointTurnsItOffAndOn() throws Exception {
    clock.pin(DEC_1);
    long plan = createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);

    setAutoSend(plan, "{\"autoSend\": false}", false);
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$[0].status").value("DRAFT"));
    verify(mailSender, never()).send(any(SimpleMailMessage.class));

    setAutoSend(plan, "{\"autoSend\": true}", true);
    mvc.perform(get("/api/recurring-plans")).andExpect(jsonPath("$[0].autoSend").value(true));
  }

  @Test
  void autoSendEndpointRejectsUnknownPlanAndMissingField() throws Exception {
    mvc.perform(
            post("/api/recurring-plans/{id}/auto-send", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"autoSend\": true}"))
        .andExpect(status().isNotFound());

    long plan = createHostingPlan(createCustomer("Lotus Bakery", "USD"), false);
    mvc.perform(
            post("/api/recurring-plans/{id}/auto-send", plan)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void mailFailureLeavesADraftMovesOnAndOtherPlansAreStillSent() throws Exception {
    clock.pin(DEC_1);
    long broken = createCustomerWithEmail("Broken Mail Co", "fail@example.com");
    long fine = createCustomerWithEmail("Fine Mail Co", "ok@example.com");
    createHostingPlan(broken, true);
    createHostingPlan(fine, true);
    doAnswer(
            call -> {
              SimpleMailMessage message = call.getArgument(0);
              if ("fail@example.com".equals(message.getTo()[0])) {
                throw new MailSendException("mail server down");
              }
              return null;
            })
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    String body =
        mvc.perform(post("/api/recurring-plans/run"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    java.util.List<String> brokenStatus =
        JsonPath.read(body, "$[?(@.customer.id == " + broken + ")].status");
    java.util.List<String> fineStatus =
        JsonPath.read(body, "$[?(@.customer.id == " + fine + ")].status");
    assertThat(brokenStatus).containsExactly("DRAFT");
    assertThat(fineStatus).containsExactly("SENT");
    assertThat(
            jdbc.queryForObject(
                "SELECT sent_at FROM invoices WHERE customer_id = ?",
                java.sql.Timestamp.class,
                broken))
        .isNull();
    assertThat(
            jdbc.queryForObject(
                "SELECT sent_at FROM invoices WHERE customer_id = ?",
                java.sql.Timestamp.class,
                fine))
        .isNotNull();
    // Both plans moved from 2026-12-01 to 2027-01-01.
    mvc.perform(get("/api/recurring-plans"))
        .andExpect(jsonPath("$[0].nextRunDate").value("2027-01-01"))
        .andExpect(jsonPath("$[1].nextRunDate").value("2027-01-01"));
    // The failed invoice is a draft in the database too, so a person can still send it.
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE status = 'DRAFT' AND sent_at IS NULL",
                Integer.class))
        .isEqualTo(1);
  }

  @Test
  void missedRunIsBilledWithItsOriginalDateAndSent() throws Exception {
    clock.pin(LocalDate.of(2026, 12, 3));
    createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].issueDate").value("2026-12-01"))
        .andExpect(jsonPath("$[0].status").value("SENT"));
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  void scheduledJobSendsLikeTheRunEndpoint() throws Exception {
    clock.pin(DEC_1);
    createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);

    job.run();

    mvc.perform(get("/api/invoices"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("SENT"));
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  void pausedAutoSendPlanCreatesAndSendsNothing() throws Exception {
    clock.pin(DEC_1);
    long plan = createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);
    mvc.perform(post("/api/recurring-plans/{id}/pause", plan));

    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM invoices", Integer.class)).isZero();
  }

  @Test
  void runningTwiceOnTheSameDayEmailsAnAutoSendPlanOnlyOnce() throws Exception {
    clock.pin(DEC_1);
    createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);

    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(1)));
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));
    job.run();

    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM invoices", Integer.class)).isEqualTo(1);
  }

  @Test
  void explicitNullAutoSendOnCreateMeansFalse() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Hosting", "amount": 49.00, "autoSend": null}
            """
                .formatted(customer));
    assertThat((Boolean) JsonPath.read(body, "$.autoSend")).isFalse();
  }

  @Test
  void autoSendEndpointRejectsNullAndNonBooleanValues() throws Exception {
    long plan = createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);
    for (String json : new String[] {"{\"autoSend\": null}", "{\"autoSend\": \"maybe\"}", ""}) {
      mvc.perform(
              post("/api/recurring-plans/{id}/auto-send", plan)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(json))
          .andExpect(status().isBadRequest());
    }
    // The failed calls left the flag as it was.
    mvc.perform(get("/api/recurring-plans")).andExpect(jsonPath("$[0].autoSend").value(true));
  }

  @Test
  void settingTheSameAutoSendValueTwiceIsHarmless() throws Exception {
    long plan = createHostingPlan(createCustomer("Lotus Bakery", "USD"), true);
    setAutoSend(plan, "{\"autoSend\": true}", true);
    setAutoSend(plan, "{\"autoSend\": true}", true);
  }

  @Test
  void taxIsRoundedHalfUpOnTheSentInvoice() throws Exception {
    clock.pin(DEC_1);
    long customer = createCustomer("Lotus Bakery", "USD");
    // 0.01 at 50% tax is 0.005 of tax, which rounds half up to 0.01, so the total is 0.02.
    postJson(
        "/api/recurring-plans",
        """
        {"customerId": %d, "description": "Tiny", "amount": 0.01, "taxRate": 50.00,
         "startDate": "2026-12-01", "autoSend": true}
        """
            .formatted(customer));

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(jsonPath("$[0].tax").value(0.01))
        .andExpect(jsonPath("$[0].total").value(0.02))
        .andExpect(jsonPath("$[0].status").value("SENT"));
    ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(mail.capture());
    assertThat(mail.getValue().getText()).contains("Tax: 0.01 USD").contains("Total: 0.02 USD");
  }

  @Test
  void anAmountWithThreeDecimalsIsRejected() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    mvc.perform(
            post("/api/recurring-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"customerId": %d, "description": "Tiny", "amount": 0.005, "autoSend": true}
                    """
                        .formatted(customer)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void togglingAutoSendOnAPausedPlanKeepsItPausedAndSendsOnlyAfterResume() throws Exception {
    clock.pin(DEC_1);
    long plan = createHostingPlan(createCustomer("Lotus Bakery", "USD"), false);
    mvc.perform(post("/api/recurring-plans/{id}/pause", plan));

    mvc.perform(
            post("/api/recurring-plans/{id}/auto-send", plan)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"autoSend\": true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.autoSend").value(true))
        .andExpect(jsonPath("$.active").value(false));

    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));
    verify(mailSender, never()).send(any(SimpleMailMessage.class));

    mvc.perform(post("/api/recurring-plans/{id}/resume", plan));
    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("SENT"));
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  void whenEveryMailFailsEveryInvoiceStaysADraftAndCanBeSentByHandLater() throws Exception {
    clock.pin(DEC_1);
    createHostingPlan(createCustomerWithEmail("One Co", "one@example.com"), true);
    createHostingPlan(createCustomerWithEmail("Two Co", "two@example.com"), true);
    createHostingPlan(createCustomerWithEmail("Three Co", "three@example.com"), true);
    doAnswer(
            call -> {
              throw new MailSendException("mail server down");
            })
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].status").value("DRAFT"))
        .andExpect(jsonPath("$[1].status").value("DRAFT"))
        .andExpect(jsonPath("$[2].status").value("DRAFT"));

    // The run was not rolled back: three drafts exist and all plans moved on.
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE status = 'DRAFT' AND sent_at IS NULL"
                    + " AND recurring_plan_id IS NOT NULL",
                Integer.class))
        .isEqualTo(3);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM recurring_plans WHERE next_run_date = '2027-01-01'",
                Integer.class))
        .isEqualTo(3);

    // A second run does not retry (retry is out of scope) and sends nothing new.
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));

    // Once the mail server is back, a person can send one by hand.
    org.mockito.Mockito.reset(mailSender);
    Long draft = jdbc.queryForObject("SELECT MIN(id) FROM invoices", Long.class);
    mvc.perform(post("/api/invoices/{id}/send", draft))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SENT"));
  }

  private long createPlan(long customerId, LocalDate start) throws Exception {
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Support plan", "amount": 250.00,
             "taxRate": 10, "startDate": "%s"}
            """
                .formatted(customerId, start));
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  @Test
  void newPlanStartsOnTheFirstOfNextMonth() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    String body =
        postJson(
            "/api/recurring-plans",
            """
            {"customerId": %d, "description": "Support plan", "amount": 250.00}
            """
                .formatted(customer));

    LocalDate expected = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    assertThat((String) JsonPath.read(body, "$.nextRunDate")).isEqualTo(expected.toString());
  }

  @Test
  void duePlanCreatesADraftAndMovesOnAMonth() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    LocalDate start = LocalDate.now().minusDays(1);
    createPlan(customer, start);

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("DRAFT"))
        .andExpect(jsonPath("$[0].total").value(275.00));

    mvc.perform(get("/api/recurring-plans"))
        .andExpect(jsonPath("$[0].nextRunDate").value(start.plusMonths(1).toString()));

    // Running again the same day makes nothing new.
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void recurringInvoicesUseTheCustomersPaymentTerms() throws Exception {
    long customer = createCustomer("Net45 Co", "USD", 45);
    createPlan(customer, LocalDate.now().minusDays(1));

    mvc.perform(post("/api/recurring-plans/run"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].paymentTermsDays").value(45));
  }

  @Test
  void pausedPlansDoNotBill() throws Exception {
    long customer = createCustomer("Lotus Bakery", "USD");
    long plan = createPlan(customer, LocalDate.now());

    mvc.perform(post("/api/recurring-plans/{id}/pause", plan))
        .andExpect(jsonPath("$.active").value(false));
    mvc.perform(post("/api/recurring-plans/run")).andExpect(jsonPath("$", hasSize(0)));
  }
}
