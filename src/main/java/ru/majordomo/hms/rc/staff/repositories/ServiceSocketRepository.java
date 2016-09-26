package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

public interface ServiceSocketRepository extends MongoRepository<ServiceSocket,String> {
    List<ServiceSocket> findAll();
}
