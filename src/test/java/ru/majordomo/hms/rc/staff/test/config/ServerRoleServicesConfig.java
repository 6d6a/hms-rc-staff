package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.ServerRoleRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;

@Configuration
@EnableWebMvc
public class ServerRoleServicesConfig {
    @Bean
    public GovernorOfServerRole governorOfServerRole() {
        return new GovernorOfServerRole();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public ServerRoleRestController serverRoleRestController() {
        return new ServerRoleRestController();
    }
}
