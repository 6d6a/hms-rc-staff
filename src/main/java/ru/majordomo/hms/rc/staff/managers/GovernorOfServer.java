package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServerChecks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@Component
public class GovernorOfServer extends LordOfResources<Server> {
    private ServerRepository serverRepository;
    private ServerRoleRepository serverRoleRepository;
    private Cleaner cleaner;
    private Validator validator;

    private String activeSharedHostingName;
    private String activeMailStorageName;
    private String activeMysqlDatabaseServerName;
    private String activePostgresqlDatabaseServerName;
    private String activeMailboxStorageMountPoint;

    @Value("${server.active.mail-storage.active-storage-mountpoint}")
    public void setActiveMailboxStorageMountPoint(String activeMailboxStorageMountPoint) {
        this.activeMailboxStorageMountPoint = activeMailboxStorageMountPoint;
    }

    @Value("${server.active.name.shared-hosting}")
    public void setActiveSharedHostingName(String activeSharedHostingName) {
        this.activeSharedHostingName = activeSharedHostingName;
    }

    @Value("${server.active.name.mail-storage}")
    public void setActiveMailStorageName(String activeMailStorageName) {
        this.activeMailStorageName = activeMailStorageName;
    }

    @Value("${server.active.name.mysql-database-server}")
    public void setActiveMysqlDatabaseServerName(String activeMysqlDatabaseServerName) {
        this.activeMysqlDatabaseServerName = activeMysqlDatabaseServerName;
    }

    @Value("${server.active.name.postgresql-database-server}")
    public void setActivePostgresqlDatabaseServerName(String activePostgresqlDatabaseServerName) {
        this.activePostgresqlDatabaseServerName = activePostgresqlDatabaseServerName;
    }

    @Autowired
    public void setRepository(ServerRepository repository) {
        this.serverRepository = repository;
    }

    @Autowired
    public void setRepository(ServerRoleRepository repository) {
        this.serverRoleRepository = repository;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Server createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Server server = new Server();
        try {
            LordOfResources.setResourceParams(server, serviceMessage, cleaner);

            @SuppressWarnings("unchecked") List<String> serviceIds = (List<String>) serviceMessage.getParam("serviceIds");
            server.setServiceIds(serviceIds);

            @SuppressWarnings("unchecked") List<String> storageIds = (List<String>) serviceMessage.getParam("storageIds");
            server.setStorageIds(storageIds);

            @SuppressWarnings("unchecked") List<String> serverRoleIds = (List<String>) serviceMessage.getParam("serverRoleIds");
            server.setServerRoleIds(serverRoleIds);

            isValid(server);
            save(server);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return server;
    }

    @Override
    public void isValid(Server server) throws ParameterValidateException {
        Set<ConstraintViolation<Server>> constraintViolations = validator.validate(server, ServerChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("server: " + server + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public Server build(String resourceId) throws ResourceNotFoundException {
        Server server = serverRepository.findOne(resourceId);
        if (server == null) {
            throw new ResourceNotFoundException("Server с ID:" + resourceId + " не найден");
        }

        return server;
    }

    public Server build(Map<String, String> keyValue) throws ResourceNotFoundException {
        Server server = new Server();

        Boolean byActive = false;
        Boolean byServerRole = false;
        Boolean byServiceId = false;
        Boolean findStorage = false;
        Boolean byServerId = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("state")) {
                byActive = true;
            }
            if (entry.getKey().equals("server-role")) {
                byServerRole = true;
            }
            if (entry.getKey().equals("service-id")) {
                byServiceId = true;
            }
            if (entry.getKey().equals("active-storage")) {
                findStorage = true;
            }
            if (entry.getKey().equals("server-id")) {
                byServerId = true;
            }
        }

        if (byActive && byServerRole) {
            if (!(keyValue.get("state").equals("active"))) {
                throw new ResourceNotFoundException("State указан некорректно.");
            }
            ServerRole serverRole = serverRoleRepository.findByName(keyValue.get("server-role"));
            if (serverRole == null) {
                throw new ResourceNotFoundException("ServerRole с именем: " + keyValue.get("server-role") + " не найдена");
            }

            String activeServerName;
            switch (keyValue.get("server-role")) {
                case "shared-hosting":
                    activeServerName = activeSharedHostingName;
                    break;
                case "mail-storage":
                    activeServerName = activeMailStorageName;
                    break;
                case "mysql-database-server":
                    activeServerName = activeMysqlDatabaseServerName;
                    break;
                case "postgresql-database-server":
                    activeServerName = activePostgresqlDatabaseServerName;
                    break;
                default:
                    throw new ResourceNotFoundException("По ServerRole: " + keyValue.get("server-role") + " отсутствует фильтр");
            }
            server = serverRepository.findByServerRoleIdsAndName(serverRole.getId(), activeServerName);
        }

        if (byServiceId) {
            String serviceId = keyValue.get("service-id");
            server = serverRepository.findByServiceIds(serviceId);
        }

        if (findStorage && byServerId) {
            server = build(keyValue.get("server-id"));

            server.setActiveMailboxStorageMountPoint(activeMailboxStorageMountPoint);

            return server;
        }
        return build(server.getId());
    }

    @Override
    public List<Server> buildAll(Map<String, String> keyValue) {
        List<Server> buildedServers = new ArrayList<>();

        Boolean byName = false;
        Boolean byServerId = false;
        Boolean ByServiceType = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
            if (entry.getKey().equals("serverId")) {
                byServerId = true;
            }
            if (entry.getKey().equals("service-type")) {
                ByServiceType = true;
            }
        }

        if (byName) {
            for (Server server : serverRepository.findByName(keyValue.get("name"))) {
                buildedServers.add(build(server.getId()));
            }
        }
        if (byServerId && ByServiceType) {
            List<Service> services = new ArrayList<>();

            Server server = build(keyValue.get("serverId"));
            for (Service service : server.getServices()) {
                String serviceType = service.getServiceTemplate().getServiceType().getName();
                String[] parts = serviceType.split("_");
                if (keyValue.get("service-type").toUpperCase().equals(serviceType)
                        || keyValue.get("service-type").toUpperCase().equals(parts[0])) {
                    services.add(service);
                }
            }

            server.setServices(services);

            return Collections.singletonList(server);
        }

        return buildedServers;
    }

    @Override
    public List<Server> buildAll() {
        List<Server> buildedServers = new ArrayList<>();
        for (Server server : serverRepository.findAll()) {
            buildedServers.add(build(server.getId()));
        }
        return buildedServers;
    }

    @Override
    public void save(Server resource) {
        serverRepository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {

    }

    @Override
    public void delete(String resourceId) {
        serverRepository.delete(resourceId);
    }
}
