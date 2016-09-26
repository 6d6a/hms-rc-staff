package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;

@Configuration
public class ServiceTemplateServicesConfig {
    @Bean
    public GovernorOfServiceTemplate governorOfServiceTemplate() {
        return new GovernorOfServiceTemplate();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }
}
