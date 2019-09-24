package ru.majordomo.hms.rc.staff.repositories.socket;

import org.springframework.stereotype.Repository;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.socket.Socket;

@Repository
public interface SocketRepository extends ResourceRepository<Socket, String> {
}
