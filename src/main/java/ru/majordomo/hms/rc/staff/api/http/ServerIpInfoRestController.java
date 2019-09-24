package ru.majordomo.hms.rc.staff.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ServerIpInfoRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.DTO.ServerIpInfo;
import ru.majordomo.hms.rc.staff.resources.Service;

@RestController
@RequestMapping("/server-ip-info")
public class ServerIpInfoRestController {

    private static final Logger logger = LoggerFactory.getLogger(ServerIpInfoRestController.class);

    private ServerIpInfoRepository repository;

    private ServiceRepository serviceRepository;

    @Autowired
    public void setRepository(ServerIpInfoRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setServiceRepository(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
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
            Service service = serviceRepository.findById(serviceId).orElse(null);

            if (service == null) {
                logger.error("Не найден сервис с serviceId " + serviceId);
                throw new ResourceNotFoundException("Не найден сервис с serviceId " + serviceId);
            }

            serverIpInfo = repository.findByServerId(service.getServerId());

            if (serverIpInfo == null) {
                logger.error("Не найден сервер с Id " + service.getServerId());
                throw new ResourceNotFoundException("Не найден сервер с Id " + service.getServerId());
            }

            return serverIpInfo;
        }

        serverIpInfo = repository.findByServerId(serverId);

        if (serverIpInfo == null) {
            logger.error("Не найден сервер с Id " + serverId);
            throw new ResourceNotFoundException("Не найден сервер с Id " + serverId);
        }

        return serverIpInfo;
    }
}
