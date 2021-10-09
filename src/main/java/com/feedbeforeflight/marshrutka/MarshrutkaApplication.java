package com.feedbeforeflight.marshrutka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@SpringBootApplication
@PropertySource("classpath:database.properties")
public class MarshrutkaApplication {

    private final Environment env;

    public MarshrutkaApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        SpringApplication.run(MarshrutkaApplication.class, args);
    }

}
