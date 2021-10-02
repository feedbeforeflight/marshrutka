package com.feedbeforeflight.marshrutka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class MarshrutkaApplication {

    private final Environment env;

    public MarshrutkaApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(MarshrutkaApplication.class, args);
    }

}
