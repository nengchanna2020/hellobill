package com.hellobill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelloBillApplication {

  public static void main(String[] args) {
    SpringApplication.run(HelloBillApplication.class, args);
  }
}
