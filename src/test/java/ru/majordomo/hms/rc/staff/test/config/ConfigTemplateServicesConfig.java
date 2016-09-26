package ru.majordomo.hms.rc.staff.test.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;

@Configuration
public class ConfigTemplateServicesConfig {
    @Bean
    public GovernorOfConfigTemplate governorOfConfigTemplate() {
        GovernorOfConfigTemplate governorOfConfigTemplate = new GovernorOfConfigTemplate();
        return governorOfConfigTemplate;
    }
    @Bean
    public Cleaner cleaner() {
        Cleaner cleaner = new Cleaner();
        return cleaner;
    }

    @Bean
    public ConfigTemplateRepository configTemplateRepository() {
        return Mockito.mock(ConfigTemplateRepository.class);
    }
}
