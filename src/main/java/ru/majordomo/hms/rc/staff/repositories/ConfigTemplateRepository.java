package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@Repository
public interface ConfigTemplateRepository extends ResourceRepository<ConfigTemplate,String> {
}
