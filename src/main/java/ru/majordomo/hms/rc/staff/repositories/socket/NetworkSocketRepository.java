package ru.majordomo.hms.rc.staff.repositories.socket;

import org.springframework.stereotype.Repository;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.socket.NetworkSocket;

@Repository
public interface NetworkSocketRepository extends ResourceRepository<NetworkSocket, String> {
}
