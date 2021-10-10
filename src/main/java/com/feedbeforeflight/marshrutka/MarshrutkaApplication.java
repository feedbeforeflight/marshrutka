package com.feedbeforeflight.marshrutka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@ActiveProfiles({ "production" })
public class MarshrutkaApplication {

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));

        SpringApplication.run(MarshrutkaApplication.class, args);

    }

}
