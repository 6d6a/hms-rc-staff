package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.Service;

@Repository
public interface ServiceRepository extends ResourceRepository<Service, String> {
}
