package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@Repository
public interface ServiceSocketRepository extends MongoRepository<ServiceSocket,String> {
    List<ServiceSocket> findAll();
}
