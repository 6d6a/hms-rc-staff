package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.majordomo.hms.rc.staff.api.http.ServiceTypeRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceType;

@Configuration
@EnableWebMvc
public class ServiceTypeConfig {
    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public GovernorOfServiceType governorOfServiceType() {
        return new GovernorOfServiceType();
    }

    @Bean
    public ServiceTypeRestController serviceTypeRestController() {
        return new ServiceTypeRestController();
    }
}
