package com.wex.fxpurchase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot entry point for the FX Purchase application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class FxpurchaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FxpurchaseApplication.class, args);
    }
}