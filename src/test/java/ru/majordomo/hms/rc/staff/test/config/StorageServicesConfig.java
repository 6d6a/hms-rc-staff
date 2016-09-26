package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.StorageRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfStorage;

@Configuration
@EnableWebMvc
public class StorageServicesConfig {
    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public GovernorOfStorage governorOfStorage() {
        return new GovernorOfStorage();
    }
    @Bean
    public StorageRestController storageRestController() {
        return new StorageRestController();
    }
}
