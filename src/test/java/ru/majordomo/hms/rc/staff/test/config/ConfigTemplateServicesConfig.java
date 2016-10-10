package ru.majordomo.hms.rc.staff.test.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.majordomo.hms.rc.staff.api.http.ConfigTemplateRestController;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;

@Configuration
@EnableWebMvc
public class ConfigTemplateServicesConfig {
    @Bean
    public GovernorOfConfigTemplate governorOfConfigTemplate() {
        return new GovernorOfConfigTemplate();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }

    @Bean
    public ConfigTemplateRestController configTemplateRestController() {
        return new ConfigTemplateRestController();
    }
}
