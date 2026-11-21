package com.hellobill.mail;

import com.hellobill.invoice.InvoiceResponse;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends invoice emails. In development they go to Mailpit, a fake inbox at http://localhost:8025,
 * so nothing reaches a real person.
 */
@Component
public class InvoiceMailer {

  private final JavaMailSender mailSender;
  private final String from;
  private final String businessName;

  public InvoiceMailer(
      JavaMailSender mailSender,
      @Value("${hellobill.mail-from}") String from,
      @Value("${hellobill.business-name}") String businessName) {
    this.mailSender = mailSender;
    this.from = from;
    this.businessName = businessName;
  }

  public void sendInvoice(InvoiceResponse invoice) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(invoice.customer().email());
    message.setSubject("Invoice " + invoice.number() + " from " + businessName);
    message.setText(body(invoice));
    mailSender.send(message);
  }

  String body(InvoiceResponse invoice) {
    StringBuilder text = new StringBuilder();
    text.append("Hello ").append(invoice.customer().name()).append(",\n\n");
    text.append("Here is invoice ").append(invoice.number());
    text.append(" dated ").append(invoice.issueDate()).append(".\n\n");
    for (InvoiceResponse.Item item : invoice.items()) {
      text.append("  ")
          .append(item.description())
          .append("  ")
          .append(item.quantity().stripTrailingZeros().toPlainString())
          .append(" x ")
          .append(item.unitPrice())
          .append(" = ")
          .append(item.lineTotal())
          .append('\n');
    }
    text.append('\n');
    line(text, "Subtotal", invoice.subtotal(), invoice.currency());
    if (invoice.discount().signum() > 0) {
      line(text, "Discount", invoice.discount().negate(), invoice.currency());
    }
    if (invoice.tax().signum() > 0) {
      line(text, "Tax", invoice.tax(), invoice.currency());
    }
    line(text, "Total", invoice.total(), invoice.currency());
    if (invoice.amountPaid().signum() > 0) {
      line(text, "Paid so far", invoice.amountPaid(), invoice.currency());
      line(text, "Balance due", invoice.balanceDue(), invoice.currency());
    }
    text.append("\nPlease pay within ")
        .append(invoice.paymentTermsDays())
        .append(" days, by ")
        .append(invoice.dueDate())
        .append(".\n\nThank you,\n")
        .append(businessName)
        .append('\n');
    return text.toString();
  }

  private static void line(StringBuilder text, String label, BigDecimal amount, String currency) {
    text.append(label).append(": ").append(amount.toPlainString()).append(' ').append(currency);
    text.append('\n');
  }
}
