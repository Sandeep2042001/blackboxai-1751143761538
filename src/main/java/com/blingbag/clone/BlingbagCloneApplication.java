package com.blingbag.clone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlingbagCloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlingbagCloneApplication.class, args);
    }
}
