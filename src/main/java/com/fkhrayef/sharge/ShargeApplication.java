package com.fkhrayef.sharge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShargeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShargeApplication.class, args);
    }

}
