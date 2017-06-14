package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private List<ServiceSocket> serviceSockets;
    private List<ServiceTemplate> serviceTemplates;

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

        serviceSockets = serviceSocketRepository.findAll();
        serviceTemplates = serviceTemplateRepository.findAll();

        seedTestData();

        seed();

        return true;
    }

    private void seed() {
        Map<String, List<ServiceSocket>> nginxSockets = new HashMap<>();

        serviceSockets.removeAll(serviceSockets.stream()
                .filter(serviceSocket -> serviceSocket.getName().contains("web99"))
                .collect(Collectors.toList())
        );

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
            service.setSwitchedOn(true);
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
            service.setSwitchedOn(true);
            if (serviceSocket.getId().contains("_mysql_socket")) {
                service.setId(serviceSocket.getId().replaceAll("_mysql_socket", "_mysql_service"));
            }

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
    }

    private void seedTestData() {
        Service service;

        service = new Service();
        service.setId("5821f7f796ccde0001c82a5f");
        service.setSwitchedOn(true);
        service.setName("nginx@web99");
        List<ServiceTemplate> serviceTemplatesFiltered = serviceTemplates
                .stream()
                .filter(serviceTemplate -> serviceTemplate.getName().equals("nginx"))
                .collect(Collectors.toList());

        if (!serviceTemplatesFiltered.isEmpty()) {
            service.setServiceTemplateId(serviceTemplatesFiltered.get(0).getId());
        }

        service.setServiceSocketIds(Arrays.asList("5814a90d4cedfd113e883e65", "5814a90d4cedfd113e883e64"));

        serviceRepository.save(service);

        service = new Service();
        service.setId("5824b75c96ccde0001c82a65");
        service.setSwitchedOn(true);
        service.setName("apache2-php56-default@web99");

        serviceTemplatesFiltered = serviceTemplates
                .stream()
                .filter(serviceTemplate -> serviceTemplate.getName().equals("apache2-php56-default"))
                .collect(Collectors.toList());

        if (!serviceTemplatesFiltered.isEmpty()) {
            service.setServiceTemplateId(serviceTemplatesFiltered.get(0).getId());
        }

        service.setServiceSocketIds(Collections.singletonList("5824b63c96ccde0001c82a63"));

        serviceRepository.save(service);

        service = new Service();
        service.setId("5836aea296ccde0001ddca65");
        service.setSwitchedOn(true);
        service.setName("mysql@web99");

        serviceTemplatesFiltered = serviceTemplates
                .stream()
                .filter(serviceTemplate -> serviceTemplate.getName().equals("mysql"))
                .collect(Collectors.toList());

        if (!serviceTemplatesFiltered.isEmpty()) {
            service.setServiceTemplateId(serviceTemplatesFiltered.get(0).getId());
        }

        service.setServiceSocketIds(Collections.singletonList("5835c28d96ccde0001ddca61"));

        serviceRepository.save(service);
    }
}
