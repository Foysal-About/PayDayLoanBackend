package com.paydayloan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.paydayloan"})
public class PayDayLoanApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayDayLoanApplication.class, args);
    }

}
