package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.resources.validation.group.StorageChecks;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@Service
public class GovernorOfStorage extends LordOfResources<Storage> {
    private GovernorOfServer governorOfServer;
    private Cleaner cleaner;
    private Validator validator;

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

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Storage buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        Storage storage = new Storage();

        try {
            LordOfResources.setResourceParams(storage, serviceMessage, cleaner);
            Double capacity = (Double) serviceMessage.getParam("capacity");
            Double capacityUsed = (Double) serviceMessage.getParam("capacityUsed");
            String mountPoint = (String) serviceMessage.getParam("mountPoint");

            storage.setCapacity(capacity);
            storage.setCapacityUsed(capacityUsed);
            storage.setMountPoint(mountPoint);
        } catch (ClassCastException e){
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }

        return storage;
    }

    @Override
    public void isValid(Storage storage) throws ParameterValidateException {
        Set<ConstraintViolation<Storage>> constraintViolations = validator.validate(storage, StorageChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("storage: " + storage + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public Storage build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
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
}
