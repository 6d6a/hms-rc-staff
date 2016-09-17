package ru.majordomo.hms.rc.staff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class RcStaffApllication {
    public static void main(String[] args) {
        SpringApplication.run(RcStaffApllication.class, args);
    }
}
