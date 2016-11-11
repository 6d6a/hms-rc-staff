package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.Server;

@Repository
public interface ServerRepository extends MongoRepository<Server, String> {
    List<Server> findAll();
    List<Server> findByName(String name);
    Server findByServerRoleIdAndName(String serverRoleId, String name);
    Server findByServiceId(String serviceById);
}
