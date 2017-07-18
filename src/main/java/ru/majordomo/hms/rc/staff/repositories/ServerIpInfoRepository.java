package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.majordomo.hms.rc.staff.resources.DTO.ServerIpInfo;

public interface ServerIpInfoRepository extends MongoRepository {
    ServerIpInfo findByServerId(String serverId);
}
