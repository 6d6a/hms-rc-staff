package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.Server;

@Repository
public interface ServerRepository extends ResourceRepository<Server, String> {
    Server findByServerRoleIdsAndName(String serverRoleId, String name);
    Server findByServiceIds(String serviceId);
    List<Server> findByServerRoleIds(String serverRoleId);
    @Query(value="{'serverRoleIds' : {$in: [?0]}}", fields="{name:1}")
    List<Server> findByServerRoleIdsIncludeIdAndName(String serverRoleId);
}
