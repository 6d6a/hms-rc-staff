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
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.resources.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GovernorOfServer extends LordOfResources{

    private ServerRepository serverRepository;
    private ServerRoleRepository serverRoleRepository;
    private GovernorOfServerRole governorOfServerRole;
    private GovernorOfService governorOfService;
    private GovernorOfStorage governorOfStorage;
    private Cleaner cleaner;
    private String activeSharedHostingName;
    private String activeMailStorageName;
    private String activeDatabaseServerName;

    @Value("${server.active.name.shared-hosting}")
    public void setActiveSharedHostingName(String activeSharedHostingName) {
        this.activeSharedHostingName = activeSharedHostingName;
    }

    @Value("${server.active.name.mail-storage}")
    public void setActiveMailStorageName(String activeMailStorageName) {
        this.activeMailStorageName = activeMailStorageName;
    }

    @Value("${server.active.name.database-server}")
    public void setActiveDatabaseServerName(String activeDatabaseServerName) {
        this.activeDatabaseServerName = activeDatabaseServerName;
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
    public void setGovernorOfServerRole(GovernorOfServerRole governorOfServerRole) {
        this.governorOfServerRole = governorOfServerRole;
    }

    @Autowired
    public void setGovernorOfService(GovernorOfService governorOfService) {
        this.governorOfService = governorOfService;
    }

    @Autowired
    public void setGovernorOfStorage(GovernorOfStorage governorOfStorage) {
        this.governorOfStorage = governorOfStorage;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Server server = new Server();
        try {
            LordOfResources.setResourceParams(server, serviceMessage, cleaner);

            List<Service> services = (List<Service>) serviceMessage.getParam("services");
            server.setServices(services);

            List<Storage> storages = (List<Storage>) serviceMessage.getParam("storages");
            server.setStorages(storages);

            ServerRole serverRole = (ServerRole) serviceMessage.getParam("serverRole");
            server.setServerRole(serverRole);

            isValid(server);
            save(server);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return server;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        Server server = (Server) resource;

        if (server.getServices().isEmpty() || server.getServiceIds().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один Service");
        }
        if (server.getStorages().isEmpty() || server.getStorageIds().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один Storage");
        }
        if (server.getServerRole() == null || server.getServerRole().getId() == null || server.getServerRoleId() == null) {
            throw new ParameterValidateException("Отсутствует ServerRole");
        }

        //Валидация Service
        for (Service serviceToValidate : server.getServices()) {
            Service serviceFromRepository = (Service) governorOfService.build(serviceToValidate.getId());
            if (serviceFromRepository == null) {
                throw new ParameterValidateException("Service с ID: " + serviceToValidate.getId() + " не найден");
            }
            if(!serviceFromRepository.equals(serviceToValidate)) {
                throw new ParameterValidateException("Service с ID: " + serviceToValidate.getId() + " задан некорректно");
            }
        }

        //Валидация Storage
        for (Storage storageToValidate : server.getStorages()) {
            Storage storageFromRepository = (Storage) governorOfStorage.build(storageToValidate.getId());
            if (storageFromRepository == null) {
                throw new ParameterValidateException("Storage с ID: " + storageToValidate.getId() + " не найден");
            }
            if(!storageFromRepository.equals(storageToValidate)) {
                throw new ParameterValidateException("Storage с ID: " + storageToValidate.getId() + " задан некорректно");
            }
        }

        //Валидация ServerRole
        ServerRole serverRoleToValidate = server.getServerRole();
        ServerRole serverRoleFromRepository = (ServerRole) governorOfServerRole.build(serverRoleToValidate.getId());
        if (serverRoleFromRepository == null) {
            throw new ParameterValidateException("ServerRole с ID: " + serverRoleToValidate.getId() + " не найден");
        }
        if(!serverRoleFromRepository.equals(serverRoleToValidate)) {
            throw new ParameterValidateException("ServerRole с ID: " + serverRoleToValidate.getId() + " задан некорректно");
        }
    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        Server server = serverRepository.findOne(resourceId);
        if (server == null) {
            throw new ResourceNotFoundException("Server с ID:" + resourceId + " не найден");
        }

        ServerRole serverRole = (ServerRole) governorOfServerRole.build(server.getServerRoleId());
        server.setServerRole(serverRole);

        for (String serviceId : server.getServiceIds()) {
            Service service = (Service) governorOfService.build(serviceId);
            server.addService(service);
        }

        for (String storageId : server.getStorageIds()) {
            Storage storage = (Storage) governorOfStorage.build(storageId);
            server.addStorage(storage);
        }

        return server;
    }

    public Resource build(Map<String, String> keyValue) throws ResourceNotFoundException {

        Server server = new Server();

        Boolean byActive = false;
        Boolean byServerRole = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("state")) {
                byActive = true;
            }
            if (entry.getKey().equals("server-role")) {
                byServerRole = true;
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
                case "database-server":
                    activeServerName = activeDatabaseServerName;
                    break;
                default:
                    throw new ResourceNotFoundException("По ServerRole: " + keyValue.get("server-role") + " отсутствует фильтр");
            }
            server = serverRepository.findByServerRoleIdAndName(serverRole.getId(), activeServerName);
        }
        return build(server.getId());
    }

    @Override
    public List<? extends Resource> buildAll(Map<String, String> keyValue) {
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
                buildedServers.add((Server) build(server.getId()));
            }
        }
        if (byServerId && ByServiceType) {
            List<Service> services = new ArrayList<>();

            Server server = (Server) build(keyValue.get("serverId"));
            for (Service service : server.getServices()) {
                String[] parts = service.getServiceType().getName().split("_");
                if (keyValue.get("service-type").toUpperCase().equals(parts[0])) {
                    services.add(service);
                }
            }

            return services;
        }

        return buildedServers;
    }

    @Override
    public List<Server> buildAll() {
        List<Server> buildedServers = new ArrayList<>();
        for (Server server : serverRepository.findAll()) {
            buildedServers.add((Server) build(server.getId()));
        }
        return buildedServers;
    }

    @Override
    public void save(Resource resource) {
        serverRepository.save((Server) resource);
    }

    @Override
    public void delete(String resourceId) {
        serverRepository.delete(resourceId);
    }

}
