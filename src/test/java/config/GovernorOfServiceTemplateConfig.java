package config;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.majordomo.hms.rc.staff.api.amqp.Sender;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Configuration
public class GovernorOfServiceTemplateConfig {
    @Bean
    public GovernorOfServiceTemplate governorOfServiceTemplate() {
        return new GovernorOfServiceTemplate();
    }

    @Bean
    public Cleaner cleaner() {
        return new Cleaner();
    }
}
