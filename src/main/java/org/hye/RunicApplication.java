package org.hye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.hye.util")
@ComponentScan("org.hye.dao")
@ComponentScan("org.hye.controller")
public class RunicApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunicApplication.class, args);
    }

}
