package ru.majordomo.hms.rc.staff.test.managers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.test.context.junit4.SpringRunner;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfStorage;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.StorageServicesConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StorageServicesConfig.class, RepositoriesConfig.class})
public class GovernorOfStorageTest {
    @Autowired
    GovernorOfStorage governor;

    ServiceMessage testServiceMessage;
    Storage testStorage;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  Double capacity, Double capacityUsed) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("capacity", capacity);
        serviceMessage.addParam("capacityUsed", capacityUsed);
        return serviceMessage;
    }

    private Storage generateStorage(String name, Boolean switchedOn,
                                    Double capacity, Double capacityUsed) {
        Storage storage = new Storage();
        storage.setName(name);
        storage.setSwitchedOn(switchedOn);
        storage.setCapacity(capacity);
        storage.setCapacityUsed(capacityUsed);
        return storage;
    }

    @Before
    public void setUp() {
        String name = "Хранилище 1";
        Boolean switchedOn = Boolean.TRUE;
        Double capacity = 5 * Math.pow(10, 12);
        Double capacityUsed = 3 * Math.pow(10, 12);
        testServiceMessage = generateServiceMessage(name, switchedOn, capacity, capacityUsed);
        testStorage = generateStorage(name, switchedOn, capacity, capacityUsed);
    }

    @Test
    public void create() {
        try {
            Storage createdStorage = (Storage) governor.createResource(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testStorage.getName(), createdStorage.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testStorage.getSwitchedOn(), createdStorage.getSwitchedOn());
            Assert.assertEquals("Capacity не совпадает с ожидаемым", testStorage.getCapacity(), createdStorage.getCapacity());
            Assert.assertEquals("CapacityUsed не совпадает с ожидаемым", testStorage.getCapacityUsed(), createdStorage.getCapacityUsed());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
