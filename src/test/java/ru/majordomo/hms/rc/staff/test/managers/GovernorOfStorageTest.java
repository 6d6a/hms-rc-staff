package ru.majordomo.hms.rc.staff.test.managers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfStorage;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.StorageServicesConfig;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StorageServicesConfig.class, RepositoriesConfig.class})
public class GovernorOfStorageTest {
    @Autowired
    GovernorOfStorage governor;

    ServiceMessage testServiceMessage;
    Storage testStorage;

    @Autowired
    private StorageRepository storageRepository;

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

    @Test
    public void build() {
        storageRepository.save(testStorage);
        Storage buildedStorage = (Storage)governor.build(testStorage.getId());
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testStorage.getName(), buildedStorage.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testStorage.getSwitchedOn(), buildedStorage.getSwitchedOn());
            Assert.assertEquals("Capacity не совпадает с ожидаемым", testStorage.getCapacity(), buildedStorage.getCapacity());
            Assert.assertEquals("CapacityUsed не совпадает с ожидаемым", testStorage.getCapacityUsed(), buildedStorage.getCapacityUsed());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        storageRepository.save(testStorage);
        List<Storage> buildedStorage = governor.build();
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testStorage.getName(), buildedStorage.get(buildedStorage.size()-1).getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testStorage.getSwitchedOn(), buildedStorage.get(buildedStorage.size()-1).getSwitchedOn());
            Assert.assertEquals("Capacity не совпадает с ожидаемым", testStorage.getCapacity(), buildedStorage.get(buildedStorage.size()-1).getCapacity());
            Assert.assertEquals("CapacityUsed не совпадает с ожидаемым", testStorage.getCapacityUsed(), buildedStorage.get(buildedStorage.size()-1).getCapacityUsed());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithInvalidCapacity() throws ParameterValidateException {
        testServiceMessage = generateServiceMessage("Хранилище 2", Boolean.TRUE, 0.0, 3 * Math.pow(10, 12));
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithInvalidCapacityUsed() throws ParameterValidateException {
        testServiceMessage = generateServiceMessage("Хранилище 3", Boolean.TRUE, 5 * Math.pow(10, 12), -10.0);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithCapacityUsedBiggerThenCapacity() throws ParameterValidateException {
        testServiceMessage = generateServiceMessage("Хранилище 4", Boolean.TRUE, 3 * Math.pow(10, 12), 5 * Math.pow(10, 12));
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithInvalidCapacity() throws ParameterValidateException {
        testStorage.setCapacity(0.0);
        governor.isValid(testStorage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithInvalidCapacityUsed() throws ParameterValidateException {
        testStorage.setCapacityUsed(-10.0);
        governor.isValid(testStorage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithInvalidCapacityUsedBiggerThenCapacity() throws ParameterValidateException {
        testStorage.setCapacity(3 * Math.pow(10, 12));
        testStorage.setCapacityUsed(5 * Math.pow(10, 12));
        governor.isValid(testStorage);
    }

}
