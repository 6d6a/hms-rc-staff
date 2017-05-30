package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Repository
public interface ServiceTemplateRepository extends ResourceRepository<ServiceTemplate,String> {
}
