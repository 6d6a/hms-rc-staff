package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.resources.Resource;

import java.util.ArrayList;
import java.util.List;

@Component
public class GovernorOfServer extends LordOfResources{

    private ServerRepository serverRepository;
    private GovernorOfServerRole governorOfServerRole;
    private GovernorOfService governorOfService;
    private GovernorOfStorage governorOfStorage;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(ServerRepository repository) {
        this.serverRepository = repository;
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

    @Override
    public List<Server> buildAll(String key) {
        List<Server> buildedServers = new ArrayList<>();
        switch (key) {
            case "": {
                for (Server server : serverRepository.findAll()) {
                    buildedServers.add((Server) build(server.getId()));
                }
                return buildedServers;
            }
            default: {
                for (Server server : serverRepository.findByName(key)) {
                    buildedServers.add((Server) build(server.getId()));
                }
                return buildedServers;
            }
        }
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
