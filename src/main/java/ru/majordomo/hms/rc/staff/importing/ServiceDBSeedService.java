package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class ServiceDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(ServiceDBSeedService.class);

    private final ServiceRepository serviceRepository;
    private final ServiceTemplateRepository serviceTemplateRepository;
    private final ServiceSocketRepository serviceSocketRepository;

    @Autowired
    public ServiceDBSeedService(
            ServiceRepository serviceRepository,
            ServiceTemplateRepository serviceTemplateRepository,
            ServiceSocketRepository serviceSocketRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.serviceTemplateRepository = serviceTemplateRepository;
        this.serviceSocketRepository = serviceSocketRepository;
    }

    public boolean seedDB() {
        serviceRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        List<ServiceSocket> serviceSockets = serviceSocketRepository.findAll();
        List<ServiceTemplate> serviceTemplates = serviceTemplateRepository.findAll();

        Map<String, List<ServiceSocket>> nginxSockets = new HashMap<>();

        for (ServiceSocket serviceSocket : serviceSockets.stream()
                .filter(serviceSocket -> serviceSocket.getName().contains("nginx"))
                .collect(Collectors.toList())
                ) {

            String[] names = serviceSocket.getName().split("@");

            if (nginxSockets.get(names[1]) == null || nginxSockets.get(names[1]).isEmpty()) {
                List<ServiceSocket> newServiceSockets = new ArrayList<>();
                newServiceSockets.add(serviceSocket);
                nginxSockets.put(names[1], newServiceSockets);
            } else {
                List<ServiceSocket> currentServiceSockets = nginxSockets.get(names[1]);
                currentServiceSockets.add(serviceSocket);
                nginxSockets.put(names[1], currentServiceSockets);
            }
            serviceSockets.remove(serviceSocket);
        }

        nginxSockets.forEach((serveName, serverServiceSockets) -> {
            Service service = new Service();
            String[] names = serverServiceSockets.get(0).getName().split("@");
            names[0] = names[0]
                    .replaceAll("-https?", "")
            ;
            service.setName(names[0] + "@" + names[1]);

            List<ServiceTemplate> serviceTemplatesFiltered = serviceTemplates
                    .stream()
                    .filter(serviceTemplate -> serviceTemplate.getName().equals(names[0]))
                    .collect(Collectors.toList());

            if (serviceTemplatesFiltered.isEmpty()) {
                logger.error("Empty serviceTemplates while searching for: " + names[0]);
            } else if (serviceTemplatesFiltered.size() > 1) {
                logger.error("serviceTemplates has size() > 1 while searching for: " + names[0]);
            } else {
                service.setServiceTemplateId(serviceTemplatesFiltered.get(0).getId());
                for (ServiceSocket oneServiceSocket : serverServiceSockets) {
                    service.addServiceSocketId(oneServiceSocket.getId());
                }
                serviceRepository.save(service);
            }

            logger.debug(service.toString());
        });

        for (ServiceSocket serviceSocket : serviceSockets) {
            Service service = new Service();
            String[] names = serviceSocket.getName().split("@");
            names[0] = names[0]
                    .replaceAll("-https?", "")
                    .replaceAll("-mysql", "")
            ;
            service.setName(names[0] + "@" + names[1]);

            List<ServiceTemplate> serviceTemplatesFiltered = serviceTemplates
                    .stream()
                    .filter(serviceTemplate -> serviceTemplate.getName().equals(names[0]))
                    .collect(Collectors.toList());

            if (serviceTemplatesFiltered.isEmpty()) {
                logger.error("Empty serviceTemplates while searching for: " + names[0]);
            } else if (serviceTemplatesFiltered.size() > 1) {
                logger.error("serviceTemplates has size() > 1 while searching for: " + names[0]);
            } else {
                service.setServiceTemplateId(serviceTemplatesFiltered.get(0).getId());
                service.addServiceSocketId(serviceSocket.getId());
                serviceRepository.save(service);
            }


            logger.debug(service.toString());
        }

//        });
    }
}
