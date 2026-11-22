package com.hellobill.recurring;

import jakarta.validation.constraints.NotNull;

/** Turns auto-send on or off for one plan. */
public record AutoSendRequest(@NotNull Boolean autoSend) {}
