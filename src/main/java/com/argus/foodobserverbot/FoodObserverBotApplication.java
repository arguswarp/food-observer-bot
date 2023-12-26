package com.argus.foodobserverbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.dev.yaml")
public class FoodObserverBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodObserverBotApplication.class, args);
    }
}
