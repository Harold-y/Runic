package org.hye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.hye.util")
public class RunicApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunicApplication.class, args);
    }

}
