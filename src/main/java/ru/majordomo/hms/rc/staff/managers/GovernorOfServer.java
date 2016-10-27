package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

import java.util.List;

@Component
public class GovernorOfServer extends LordOfResources{

    private ServerRepository serverRepository;
    private ServerRoleRepository serverRoleRepository;
    private ServiceRepository serviceRepository;
    private StorageRepository storageRepository;
    private ServiceTemplateRepository serviceTemplateRepository;
    private ConfigTemplateRepository configTemplateRepository;
    private ServiceSocketRepository serviceSocketRepository;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(ServerRepository repository) {
        this.serverRepository = repository;
    }

    @Autowired
    public void setTemplateRepository(ServerRoleRepository serverRoleRepository) {
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
    public void setServiceTemplateRepository(ServiceTemplateRepository serviceTemplateRepository) {
        this.serviceTemplateRepository = serviceTemplateRepository;
    }

    @Autowired
    public void setConfigTemplateRepository(ConfigTemplateRepository configTemplateRepository) {
        this.configTemplateRepository = configTemplateRepository;
    }

    @Autowired
    public void setServiceSocketRepository(ServiceSocketRepository serviceSocketRepository) {
        this.serviceSocketRepository = serviceSocketRepository;
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
            serverRepository.save(server);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return server;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        Server server = (Server) resource;
        if (server.getServices().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один Service");
        }
        if (server.getStorages().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один Storage");
        }
        if (server.getServerRole() == null || server.getServerRole().getId() == null) {
            throw new ParameterValidateException("Отсутствует ServerRole");
        }
        for (String serviceId: server.getServiceIds()) {
            Service service = serviceRepository.findOne(serviceId);
            if (service == null) {
                throw new ParameterValidateException("Service с ID: " + serviceId + " не найден");
            }
        }
        for (String storageId: server.getStorageIds()) {
            Storage storage = storageRepository.findOne(storageId);
            if (storage == null) {
                throw new ParameterValidateException("Storage с ID: " + storageId + " не найден");
            }
        }
        String serverRoleId = server.getServerRoleId();
        ServerRole serverRole = serverRoleRepository.findOne(serverRoleId);
        if (serverRole == null) {
            throw new ParameterValidateException("ServerRole с ID: " + serverRoleId + " не найден");
        }
    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        Server server = serverRepository.findOne(resourceId);
        if (server == null) {
            throw new ResourceNotFoundException("Server с ID:" + resourceId + " не найден");
        }

        ServerRole serverRole = serverRoleRepository.findOne(server.getServerRoleId());
        if (serverRole == null) {
            throw new ResourceNotFoundException("ServerRole с ID:" + server.getServerRoleId() + " не найден");
        }
        for (String serviceTemplateId: serverRole.getServiceTemplateIds()) {
            ServiceTemplate serviceTemplate = serviceTemplateRepository.findOne(serviceTemplateId);
            if (serviceTemplate == null) {
                throw new ResourceNotFoundException("ServiceTemplate с ID:" + serviceTemplateId + "не найден");
            }

            for (String configTemplateId: serviceTemplate.getConfigTemplateIds()) {
                ConfigTemplate configTemplate = configTemplateRepository.findOne(configTemplateId);
                if (configTemplate == null) {
                    throw new ResourceNotFoundException("ConfigTemplate с ID:" + configTemplateId + "не найден");
                }
                serviceTemplate.addConfigTemplate(configTemplate);
            }

            serverRole.addServiceTemplate(serviceTemplate);
        }
        server.setServerRole(serverRole);

        for (String serviceId : server.getServiceIds()) {
            Service service = serviceRepository.findOne(serviceId);
            if (service == null) {
                throw new ResourceNotFoundException("Service с ID:" + serviceId + " не найден");
            }
            for (String serviceSocketId: service.getServiceSocketIds()) {
                ServiceSocket serviceSocket = serviceSocketRepository.findOne(serviceSocketId);
                if (serviceSocket == null) {
                    throw new ResourceNotFoundException("ServiceSocket с ID:" + serviceSocketId + "не найден");
                }
                service.addServiceSocket(serviceSocket);
            }

            ServiceTemplate serviceTemplate = serviceTemplateRepository.findOne(service.getServiceTemplateId());
            if (serviceTemplate == null) {
                throw new ResourceNotFoundException("ServiceTemplate с ID:" + service.getServiceTemplateId() + "не найден");
            }

            for (String configTemplateId: serviceTemplate.getConfigTemplateIds()) {
                ConfigTemplate configTemplate = configTemplateRepository.findOne(configTemplateId);
                if (configTemplate == null) {
                    throw new ResourceNotFoundException("ConfigTemplate с ID:" + configTemplateId + "не найден");
                }
                serviceTemplate.addConfigTemplate(configTemplate);
            }

            service.setServiceTemplate(serviceTemplate);
            server.addService(service);
        }

        for (String storageId : server.getStorageIds()) {
            if (storageRepository.findOne(storageId) == null) {
                throw new ResourceNotFoundException("Storage с ID:" + storageId + "не найден");
            }
            server.addStorage(storageRepository.findOne(storageId));
        }

        return server;
    }
}
