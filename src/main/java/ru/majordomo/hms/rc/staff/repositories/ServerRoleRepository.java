package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.ServerRole;

@Repository
public interface ServerRoleRepository extends ResourceRepository<ServerRole, String> {
}
