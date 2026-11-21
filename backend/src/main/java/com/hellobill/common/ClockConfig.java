package com.hellobill.common;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One clock for the whole app, so tests can pin "today". */
@Configuration
public class ClockConfig {

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }
}
