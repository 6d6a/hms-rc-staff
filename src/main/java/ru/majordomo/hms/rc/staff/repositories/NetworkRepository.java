package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.Network;

public interface NetworkRepository extends MongoRepository<Network, String> {
    List<Network> findAll();
}
