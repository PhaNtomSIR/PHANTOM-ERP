package com.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
        System.out.println("=========================================================");
        System.out.println(" Enterprise Resource Planning (ERP) System Started!");
        System.out.println(" Web Portal: http://localhost:8080");
        System.out.println(" H2 Central DB Console: http://localhost:8080/h2-console");
        System.out.println("=========================================================");
    }
}
