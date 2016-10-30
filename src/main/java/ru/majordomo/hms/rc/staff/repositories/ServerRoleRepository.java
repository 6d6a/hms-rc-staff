package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ServerRole;

@Repository
public interface ServerRoleRepository extends MongoRepository<ServerRole, String> {
    List<ServerRole> findAll();
    List<ServerRole> findByName(String name);
}
