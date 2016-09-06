package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

public interface ServiceTemplateRepository extends MongoRepository<ServiceTemplate,String> {
    List<ServiceTemplate> findAll();
}
