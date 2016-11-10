package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceType;

import java.util.List;

public interface ServiceTypeRepository extends MongoRepository<ServiceType, String> {
    List<ServiceType> findAll();
    ServiceType findByName(String name);
    Long deleteServiceTypeByName(String name);
}
