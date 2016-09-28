package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.resources.Service;

public interface ServiceRepository extends MongoRepository<Service, String> {
    List<Service> findAll();
}
