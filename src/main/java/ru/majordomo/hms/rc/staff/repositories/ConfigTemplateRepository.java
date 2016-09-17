package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

public interface ConfigTemplateRepository extends MongoRepository<ConfigTemplate,String> {
    List<ConfigTemplate> findAll();
}
