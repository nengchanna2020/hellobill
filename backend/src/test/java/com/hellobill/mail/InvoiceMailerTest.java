package com.hellobill.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.hellobill.invoice.InvoiceResponse;
import com.hellobill.invoice.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class InvoiceMailerTest {

  private final InvoiceMailer mailer =
      new InvoiceMailer(mock(JavaMailSender.class), "billing@test.local", "Test Co.");

  @Test
  void bodyListsItemsAndTotal() {
    InvoiceResponse invoice =
        new InvoiceResponse(
            1L,
            "INV-0007",
            InvoiceStatus.DRAFT,
            new InvoiceResponse.CustomerSummary(1L, "Lotus Bakery", "a@example.com"),
            "USD",
            LocalDate.of(2026, 3, 1),
            30,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            null,
            null,
            List.of(
                new InvoiceResponse.Item(
                    "Website maintenance",
                    new BigDecimal("1.00"),
                    new BigDecimal("450.00"),
                    new BigDecimal("450.00"))),
            new BigDecimal("450.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("450.00"),
            new BigDecimal("0.00"),
            new BigDecimal("450.00"),
            List.of());

    String body = mailer.body(invoice);

    assertThat(body)
        .contains("Hello Lotus Bakery")
        .contains("INV-0007")
        .contains("Website maintenance  1 x 450.00 = 450.00")
        .contains("Total: 450.00 USD")
        .contains("within 30 days")
        .doesNotContain("Discount");
  }
}
