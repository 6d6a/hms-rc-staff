package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.ServerRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;

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
}
