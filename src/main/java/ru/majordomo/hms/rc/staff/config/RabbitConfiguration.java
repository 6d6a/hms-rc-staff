package ru.majordomo.hms.rc.staff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;

@Configuration
public class RabbitConfiguration {

    @Bean
    Queue accountCreateQueue() {
        return new Queue();
    }

}
