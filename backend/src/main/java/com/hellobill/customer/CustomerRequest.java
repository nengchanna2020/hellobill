package com.hellobill.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Email String email,
    /** Optional. Digits with spaces, dashes, dots, brackets and a leading plus. Blank clears it. */
    @Size(max = 30)
        @Pattern(
            regexp = "\\s*|\\s*\\+?[0-9][0-9 ().-]{3,28}[0-9]\\s*",
            message = "must be a valid phone number")
        String phone,
    @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "must be a 2-letter country code")
        String country,
    @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter currency code")
        String currency,
    /** Left out means 30 on create, and "unchanged" on update. */
    @Min(0) @Max(365) Integer defaultPaymentTermsDays) {}
