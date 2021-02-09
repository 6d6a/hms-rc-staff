package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.majordomo.hms.rc.staff.api.amqp.ServiceAmqpController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.*;

@Configuration
public class ConfigOfGovernors {

    @Bean
    public GovernorOfConfigTemplate governorOfConfigTemplate() {
        return new GovernorOfConfigTemplate();
    }
    @Bean
    public GovernorOfNetwork governorOfNetwork() {
        return new GovernorOfNetwork();
    }
    @Bean
    public GovernorOfServerRole governorOfServerRole() {
        return new GovernorOfServerRole();
    }
    @Bean
    public GovernorOfServer governorOfServer() {
        return new GovernorOfServer();
    }
    @Bean
    public GovernorOfService governorOfService() {
        return new GovernorOfService();
    }
    @Bean
    public GovernorOfServiceSocket governorOfServiceSocket() {
        return new GovernorOfServiceSocket();
    }
    @Bean
    public GovernorOfSocket governorOfSocket() {
        return new GovernorOfSocket();
    }
    @Bean
    public GovernorOfServiceTemplate governorOfServiceTemplate() {
        return new GovernorOfServiceTemplate();
    }
    @Bean
    public GovernorOfTemplate governorOfTemplate() {
        return new GovernorOfTemplate();
    }
    @Bean
    public GovernorOfServiceType governorOfServiceType() {
        return new GovernorOfServiceType();
    }
    @Bean
    public GovernorOfStorage governorOfStorage() {
        return new GovernorOfStorage();
    }
    @MockBean
    ServiceAmqpController serviceAmqpController;
    public ServiceAmqpController serviceAmqpController() {
        return serviceAmqpController;
    }
    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }
}
