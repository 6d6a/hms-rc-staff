package ru.majordomo.hms.rc.staff.importing;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Service
public class ServerRoleDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(ServerRoleDBSeedService.class);

    private final ServerRoleRepository serverRoleRepository;
    private final ServiceTemplateRepository serviceTemplateRepository;

    @Autowired
    public ServerRoleDBSeedService(
            ServerRoleRepository serverRoleRepository,
            ServiceTemplateRepository serviceTemplateRepository
    ) {
        this.serverRoleRepository = serverRoleRepository;
        this.serviceTemplateRepository = serviceTemplateRepository;
    }

    public boolean seedDB() {
        serverRoleRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        Map<String, List<String>> serverRoleNamesWithServiceTemplates = ImmutableMap.<String, List<String>>builder()
                .put("shared-hosting", Arrays.asList(
                        "nginx",
                        "apache2-php56-default"
                        )
                )
                .put("mail-storage", Collections.emptyList())
                .put("mysql-database-server", Collections.singletonList("mysql"))
                .build();

        List<ServiceTemplate> serviceTemplates = serviceTemplateRepository.findAll();

        serverRoleNamesWithServiceTemplates.forEach((name, serviceTemplateNames) -> {
            ServerRole serverRole = new ServerRole();
            serverRole.setName(name);

            List<String> serviceTemplateIds = new ArrayList<>();

            for (String serviceTemplateName : serviceTemplateNames) {
                serviceTemplateIds.addAll(
                        serviceTemplates
                                .stream()
                                .filter(serviceTemplate -> serviceTemplate.getName().equals(serviceTemplateName))
                                .map(Resource::getId)
                                .collect(Collectors.toList())
                );
            }

            serverRole.setServiceTemplateIds(serviceTemplateIds);

            serverRoleRepository.save(serverRole);

            logger.debug(serverRole.toString());
        });
    }
}
