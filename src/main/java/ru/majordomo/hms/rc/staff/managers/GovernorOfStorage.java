package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GovernorOfStorage extends LordOfResources<Storage> {

    private StorageRepository repository;
    private GovernorOfServer governorOfServer;
    private Cleaner cleaner;

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setRepository(StorageRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServer(GovernorOfServer governorOfServer) {
        this.governorOfServer = governorOfServer;
    }

    private static final Logger logger = LoggerFactory.getLogger(GovernorOfStorage.class);

    @Override
    public Storage createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        Storage storage = new Storage();
        storage = (Storage) LordOfResources.setResourceParams(storage, serviceMessage, cleaner);

        try {
            Double capacity = (Double) serviceMessage.getParam("capacity");
            if (capacity <= 0) {
                throw new ParameterValidateException("capacity не может быть меньше или равен нулю");
            }
            Double capacityUsed = (Double) serviceMessage.getParam("capacityUsed");
            if (capacityUsed < 0) {
                throw new ParameterValidateException("capacityUsed не может быть меньше нуля");
            }
            if (capacityUsed > capacity) {
                throw new ParameterValidateException("capacityUsed не может быть больше capacity");
            }
            String mountPoint = (String) serviceMessage.getParam("mountPoint");
            if (mountPoint == null) {
                throw new ParameterValidateException("Нужен mountPoint");
            }
            if (!mountPoint.startsWith("/")) {
                throw new ParameterValidateException("Путь должен быть абсолютным");
            }
            storage.setCapacity(capacity);
            storage.setCapacityUsed(capacityUsed);
            storage.setMountPoint(mountPoint);

            save(storage);
        } catch (ClassCastException e){
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }

        return storage;
    }

    @Override
    public void isValid(Storage resource) throws ParameterValidateException {
        Double capacity = resource.getCapacity();
        Double capacityUsed = resource.getCapacityUsed();

        if (capacity <= 0) {
            throw new ParameterValidateException("capacity не может быть меньше или равен нулю");
        }

        if (capacityUsed < 0) {
            throw new ParameterValidateException("capacityUsed не может быть меньше нуля");
        }

        if (capacityUsed > capacity) {
            throw new ParameterValidateException("capacityUsed не может быть больше capacity");
        }
    }

    @Override
    public Storage build(String resourceId) throws ResourceNotFoundException {
        Storage storage = repository.findOne(resourceId);
        if (storage == null) {
            throw new ResourceNotFoundException("Storage с ID:" + resourceId + " не найден");
        }
        return storage;
    }

    @Override
    public Storage build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<Storage> buildAll(Map<String, String> keyValue) {

        List<Storage> buildedStorages = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (Storage storage : repository.findByName(keyValue.get("name"))) {
                buildedStorages.add(build(storage.getId()));
            }
        } else {
            for (Storage storage : repository.findAll()) {
                buildedStorages.add(build(storage.getId()));
            }
        }

        return buildedStorages;
    }

    @Override
    public List<Storage> buildAll() {
        return repository.findAll();
    }

    @Override
    public void save(Storage resource) {
        repository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<Server> servers = governorOfServer.buildAll();
        for (Server server : servers) {
            List<Storage> storages = server.getStorages();
            for (Storage storage : storages) {
                if (storage.getId().equals(resourceId)) {
                    throw new ParameterValidateException("Я нашла Server с ID " + server.getId()
                            + ", именуемый " + server.getName() + ", так вот в нём имеется удаляемый Storage");
                }
            }
        }

    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        repository.delete(resourceId);
    }

}
