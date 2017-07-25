package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.majordomo.hms.rc.staff.resources.DTO.ServerIpInfo;

@Repository
public interface ServerIpInfoRepository extends MongoRepository<ServerIpInfo, String> {
    ServerIpInfo findByServerId(String serverId);
}
