package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServerChecks;

@Component
@RefreshScope
public class GovernorOfServer extends LordOfResources<Server> {
    private ServerRoleRepository serverRoleRepository;
    private ServiceRepository serviceRepository;
    private StorageRepository storageRepository;
    private Cleaner cleaner;
    private Validator validator;

    private String activeSharedHostingName;
    private String activeMailStorageName;
    private String activeMjMailStorageName;
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

    @Value("${server.active.name.mj-mail-storage}")
    public void setActiveMjMailStorageName(String activeMjMailStorageName) {
        this.activeMjMailStorageName = activeMjMailStorageName;
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
        this.repository = repository;
    }

    @Autowired
    public void setServerRoleRepository(ServerRoleRepository serverRoleRepository) {
        this.serverRoleRepository = serverRoleRepository;
    }

    @Autowired
    public void setServiceRepository(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Autowired
    public void setStorageRepository(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
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
    public Server buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        Server server = new Server();

        try {
            LordOfResources.setResourceParams(server, serviceMessage, cleaner);

            @SuppressWarnings("unchecked") List<String> serviceIds = (List<String>) serviceMessage.getParam("serviceIds");
            server.setServiceIds(serviceIds);

            for (String serviceId : serviceIds) {
                serviceRepository.findById(serviceId).ifPresent(server::addService);
            }

            @SuppressWarnings("unchecked") List<String> storageIds = (List<String>) serviceMessage.getParam("storageIds");
            server.setStorageIds(storageIds);

            for (String storageId : storageIds) {
                storageRepository.findById(storageId).ifPresent(server::addStorage);
            }

            @SuppressWarnings("unchecked") List<String> serverRoleIds = (List<String>) serviceMessage.getParam("serverRoleIds");
            server.setServerRoleIds(serverRoleIds);
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

    public Server build(Map<String, String> keyValue) throws ResourceNotFoundException {
        Server server = new Server();

        Boolean byActive = false;
        Boolean byServerRole = false;
        Boolean byServiceId = false;
        Boolean findStorage = false;
        Boolean byServerId = false;
        Boolean byMj = false;

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
            if (entry.getKey().equals("mj")) {
                byMj = true;
            }
        }

        if (byActive && byServerRole) {
            if (!(keyValue.get("state").equals("active"))) {
                throw new ResourceNotFoundException("State указан некорректно.");
            }
            ServerRole serverRole = serverRoleRepository.findOneByName(keyValue.get("server-role"));
            if (serverRole == null) {
                throw new ResourceNotFoundException("ServerRole с именем: " + keyValue.get("server-role") + " не найдена");
            }

            String activeServerName;
            switch (keyValue.get("server-role")) {
                case "shared-hosting":
                    activeServerName = activeSharedHostingName;
                    break;
                case "mail-storage":
                    activeServerName = byMj ? activeMjMailStorageName : activeMailStorageName;
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
            server = ((ServerRepository) repository).findByServerRoleIdsAndName(serverRole.getId(), activeServerName);
        }

        if (byServiceId) {
            String serviceId = keyValue.get("service-id");
            Service service = serviceRepository.findById(serviceId).orElseThrow(
                    () -> new ResourceNotFoundException("Service с serviceId: " + serviceId + " не найден")
            );
            server = repository.findById(service.getServerId()).orElseThrow(
                    () -> new ResourceNotFoundException("Server по serviceId: " + serviceId + " не найден")
            );
        }

        if (findStorage && byServerId) {
            server = build(keyValue.get("server-id"));

            server.setActiveMailboxStorageMountPoint(activeMailboxStorageMountPoint);
        }

        return server;
    }

    @Override
    public List<Server> buildAllOnlyIdAndName(Map<String, String> keyValue) {
        if (keyValue.get("name") != null) {
            return repository.findByNameOnlyIdAndName(keyValue.get("name"));
        } else if (keyValue.get("server-role") != null) {
            ServerRole serverRole = serverRoleRepository.findOneByName(keyValue.get("server-role"));

            if (serverRole == null) {
                throw new ResourceNotFoundException("ServerRole с именем: " + keyValue.get("server-role") + " не найдена");
            }

            return ((ServerRepository) repository).findByServerRoleIdsIncludeIdAndName(serverRole.getId());
        } else {
            return repository.findAll();
        }
    }

    @Override
    public List<Server> buildAll(Map<String, String> keyValue) {
        if (keyValue.get("name") != null) {
            if (keyValue.get("regex") != null) {
                return repository.findByNameRegEx(keyValue.get("name"));
            }
            return repository.findByName(keyValue.get("name"));
        } else if (keyValue.get("server-role") != null) {
            ServerRole serverRole = serverRoleRepository.findOneByName(keyValue.get("server-role"));

            if (serverRole == null) {
                throw new ResourceNotFoundException("ServerRole с именем: " + keyValue.get("server-role") + " не найдена");
            }

            return ((ServerRepository) repository).findByServerRoleIds(serverRole.getId());
        } else {
            return repository.findAll();
        }
    }
}
