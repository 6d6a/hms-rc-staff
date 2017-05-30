package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.Server;

@Repository
public interface ServerRepository extends ResourceRepository<Server, String> {
    Server findByServerRoleIdsAndName(String serverRoleId, String name);
    Server findByServiceIds(String serviceId);
}
