package org.hye;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.hye.util")
@ComponentScan("org.hye.config")
@MapperScan(basePackages = "org.hye.dao")
@ComponentScan("org.hye.service")
@ComponentScan("org.hye.controller")
public class RunicApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunicApplication.class, args);
    }

}
