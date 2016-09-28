package ru.majordomo.hms.rc.staff.test.config;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.ServiceRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;

@Configuration
@EnableWebMvc
public class ServiceServicesConfig {
    @Bean
    public GovernorOfService governorOfService() {
        return new GovernorOfService();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public ServiceRestController serviceRestController() {
        return new ServiceRestController();
    }
}
