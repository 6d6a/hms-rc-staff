package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.ServiceSocketRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;

@Configuration
@EnableWebMvc
public class ServiceSocketServicesConfig {
    @Bean
    public GovernorOfServiceSocket governorOfServiceSocket() {
        return new GovernorOfServiceSocket();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public ServiceSocketRestController serviceSocketRestController() {
        return new ServiceSocketRestController();
    }
}
