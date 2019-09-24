package ru.majordomo.hms.rc.staff.repositories.socket;

import org.springframework.stereotype.Repository;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.socket.UnixSocket;

@Repository
public interface UnixSocketRepository extends ResourceRepository<UnixSocket, String> {
}
