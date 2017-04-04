package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.Storage;

@Repository
public interface StorageRepository extends MongoRepository<Storage,String> {
    List<Storage> findAll();
    List<Storage> findByName(String name);
}
