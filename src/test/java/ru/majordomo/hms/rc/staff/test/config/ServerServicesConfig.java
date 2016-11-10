package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.ServerRestController;
import ru.majordomo.hms.rc.staff.api.http.ServiceTypeRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.*;

@Configuration
@EnableWebMvc
public class ServerServicesConfig {
    @Bean
    public GovernorOfServer governorOfServer() {
        return new GovernorOfServer();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public ServerRestController serverRestController() {
        return new ServerRestController();
    }

    @Bean
    public GovernorOfServerRole governorOfServerRole() {
        return new GovernorOfServerRole();
    }

    @Bean
    public GovernorOfService governorOfService() {
        return new GovernorOfService();
    }

    @Bean
    public GovernorOfServiceTemplate governorOfServiceTemplate() {
        return new GovernorOfServiceTemplate();
    }

    @Bean
    public GovernorOfConfigTemplate governorOfConfigTemplate() {
        return new GovernorOfConfigTemplate();
    }

    @Bean
    public GovernorOfServiceSocket governorOfServiceSocket() {
        return new GovernorOfServiceSocket();
    }

    @Bean
    public GovernorOfStorage governorOfStorage() {
        return new GovernorOfStorage();
    }

    @Bean
    public ServiceTypeRestController serviceTypeRestController() {
        return new ServiceTypeRestController();
    }

    @Bean
    public GovernorOfServiceType governorOfServiceType() {
        return new GovernorOfServiceType();
    }
}
