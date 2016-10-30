package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Repository
public interface ServiceTemplateRepository extends MongoRepository<ServiceTemplate,String> {
    List<ServiceTemplate> findAll();
    List<ServiceTemplate> findByName(String name);
}
