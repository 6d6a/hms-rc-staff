package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

import java.util.List;

@Component
public class GovernorOfServer extends LordOfResources{

    private ServerRepository serverRepository;
    private ServerRoleRepository serverRoleRepository;
    private ServiceRepository serviceRepository;
    private StorageRepository storageRepository;
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
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Server server = new Server();
        try {
            LordOfResources.setResourceParams(server, serviceMessage, cleaner);
            List<String> serviceIds = cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("serviceIds"));
            setServiceListByIds(server, serviceIds);
            if (server.getServices().isEmpty()) {
                throw new ParameterValidateException("Должен быть задан хотя бы один service");
            }
            List<String> storageIds = cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("storageIds"));
            setStorageListByIds(server, storageIds);
            if (server.getStorages().isEmpty()) {
                throw new ParameterValidateException("Должен быть задан хотя бы один storage");
            }
            String serverRoleId = cleaner.cleanString((String) serviceMessage.getParam("serverRoleId"));
            setServerRoleById(server, serverRoleId);
            isValid(server);
            serverRepository.save(server);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return server;
    }

    public void setServiceListByIds(Server server, List<String> serviceIdList) throws ParameterValidateException {
        server.setServices((List<Service>) serviceRepository.findAll(serviceIdList));
    }

    public void setStorageListByIds(Server server, List<String> storageIdList) throws ParameterValidateException {
        server.setStorages((List<Storage>) storageRepository.findAll(storageIdList));
    }

    public void setServerRoleById (Server server, String serverRoleId) throws ParameterValidateException {
            server.setServerRole(serverRoleRepository.findOne(serverRoleId));
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
}
