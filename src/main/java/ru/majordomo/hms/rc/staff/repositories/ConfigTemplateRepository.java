package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@Repository
public interface ConfigTemplateRepository extends MongoRepository<ConfigTemplate,String> {
    List<ConfigTemplate> findAll();
    List<ConfigTemplate> findByName(String name);
}
