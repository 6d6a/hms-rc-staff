package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Storage;

//TODO
@Service
public class GovernorOfStorage extends LordOfResources {

    @Autowired
    StorageRepository repository;

    @Autowired
    Cleaner cleaner;

    private static final Logger logger = LoggerFactory.getLogger(GovernorOfStorage.class);

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
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
            storage.setCapacity(capacity);
            storage.setCapacityUsed(capacityUsed);

            repository.save(storage);
        } catch (ClassCastException e){
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }

        return storage;
    }
}
