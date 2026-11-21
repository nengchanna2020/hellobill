package com.hellobill.invoice;

import java.math.BigDecimal;

public record InvoiceTotals(
    BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total) {}
