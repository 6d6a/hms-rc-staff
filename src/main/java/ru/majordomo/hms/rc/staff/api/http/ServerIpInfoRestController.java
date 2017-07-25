package ru.majordomo.hms.rc.staff.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.majordomo.hms.rc.staff.repositories.ServerIpInfoRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.resources.DTO.ServerIpInfo;
import ru.majordomo.hms.rc.staff.resources.Server;

@RestController
@RequestMapping("/server-ip-info")
public class ServerIpInfoRestController {

    private static final Logger logger = LoggerFactory.getLogger(ServerIpInfoRestController.class);

    private ServerIpInfoRepository repository;

    private ServerRepository serverRepository;

    @Autowired
    public void setRepository(ServerIpInfoRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setServerRepository(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ServerIpInfo getInfo(
            @RequestParam(value = "serverId", required = false) String serverId,
            @RequestParam(value = "serviceId", required = false) String serviceId
    ) {
        ServerIpInfo serverIpInfo = new ServerIpInfo();

        if ((serverId == null || serverId.equals("")) && (serviceId == null || serviceId.equals(""))) {
            logger.error("Необходимо указать serverId или serviceId");
            throw new ResourceNotFoundException("Необходимо указать serverId или serviceId");
        }

        if (serviceId != null && !serviceId.equals("")) {
            Server server = this.serverRepository.findByServiceIds(serviceId);
            if (server == null) {
                logger.error("Не найден сервер с serviceId " + serviceId);
                throw new ResourceNotFoundException("Не найден сервер с serviceId " + serviceId);
            }
            serverIpInfo = this.repository.findByServerId(server.getId());
            if (serverIpInfo == null) {
                logger.error("Не найден сервер с Id " + server.getId());
                throw new ResourceNotFoundException("Не найден сервер с Id " + server.getId());
            }
            return serverIpInfo;
        }

        if (serverId != null && !serverId.equals("")) {
            serverIpInfo = this.repository.findByServerId(serverId);
            if (serverIpInfo == null) {
                logger.error("Не найден сервер с Id " + serverId);
                throw new ResourceNotFoundException("Не найден сервер с Id " + serverId);
            }
            return serverIpInfo;
        }

        return serverIpInfo;
    }
}
