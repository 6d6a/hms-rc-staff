package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.Storage;

@Repository
public interface StorageRepository extends ResourceRepository<Storage,String> {
}
