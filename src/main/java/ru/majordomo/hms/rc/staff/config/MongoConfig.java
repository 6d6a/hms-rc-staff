package ru.majordomo.hms.rc.staff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ru.majordomo.hms.rc.staff.config.mongo.InheritanceAwareMongoRepositoryFactoryBean;
import ru.majordomo.hms.rc.staff.config.mongo.InheritanceAwareSimpleMongoRepository;

@Configuration
@EnableMongoRepositories(
        repositoryBaseClass = InheritanceAwareSimpleMongoRepository.class,
        repositoryFactoryBeanClass = InheritanceAwareMongoRepositoryFactoryBean.class,
        basePackages = "ru.majordomo.hms.rc.staff.repositories"
)
public class MongoConfig {

}
