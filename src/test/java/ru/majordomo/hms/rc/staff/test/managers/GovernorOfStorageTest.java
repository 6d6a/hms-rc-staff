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
import ru.majordomo.hms.rc.staff.test.config.ConfigOfGovernors;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ValidationConfig;

import java.util.List;

import javax.validation.ConstraintViolationException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RepositoriesConfig.class,
        ConfigOfGovernors.class,
        ValidationConfig.class
})
public class GovernorOfStorageTest {
    @Autowired
    private GovernorOfStorage governor;

    private ServiceMessage testServiceMessage;
    private Storage testStorage;

    @Autowired
    private StorageRepository storageRepository;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  Double capacity, Double capacityUsed, String mountPoint) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("capacity", capacity);
        serviceMessage.addParam("capacityUsed", capacityUsed);
        serviceMessage.addParam("mountPoint", mountPoint);
        return serviceMessage;
    }

    private Storage generateStorage(String name, Boolean switchedOn,
                                    Double capacity, Double capacityUsed, String mountPoint) {
        Storage storage = new Storage();
        storage.setName(name);
        storage.setSwitchedOn(switchedOn);
        storage.setCapacity(capacity);
        storage.setCapacityUsed(capacityUsed);
        storage.setMountPoint(mountPoint);
        return storage;
    }

    @Before
    public void setUp() {
        String name = "Хранилище 1";
        Boolean switchedOn = Boolean.TRUE;
        Double capacity = 5 * Math.pow(10, 12);
        Double capacityUsed = 3 * Math.pow(10, 12);
        String mountPoint = "/home/";
        testServiceMessage = generateServiceMessage(name, switchedOn, capacity, capacityUsed, mountPoint);
        testStorage = generateStorage(name, switchedOn, capacity, capacityUsed, mountPoint);
    }

    @Test
    public void create() {
        try {
            Storage createdStorage = governor.create(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testStorage.getName(), createdStorage.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testStorage.getSwitchedOn(), createdStorage.getSwitchedOn());
            Assert.assertEquals("Capacity не совпадает с ожидаемым", testStorage.getCapacity(), createdStorage.getCapacity());
            Assert.assertEquals("CapacityUsed не совпадает с ожидаемым", testStorage.getCapacityUsed(), createdStorage.getCapacityUsed());
            Assert.assertEquals("mountPoint не совпадает с ожидаемым", testStorage.getMountPoint(), createdStorage.getMountPoint());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        storageRepository.save(testStorage);
        Storage buildedStorage = governor.build(testStorage.getId());
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testStorage.getName(), buildedStorage.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testStorage.getSwitchedOn(), buildedStorage.getSwitchedOn());
            Assert.assertEquals("Capacity не совпадает с ожидаемым", testStorage.getCapacity(), buildedStorage.getCapacity());
            Assert.assertEquals("CapacityUsed не совпадает с ожидаемым", testStorage.getCapacityUsed(), buildedStorage.getCapacityUsed());
            Assert.assertEquals("mountPoint не совпадает с ожидаемым", testStorage.getMountPoint(), buildedStorage.getMountPoint());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        storageRepository.save(testStorage);
        List<Storage> buildedStorage = governor.buildAll();
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testStorage.getName(), buildedStorage.get(buildedStorage.size()-1).getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testStorage.getSwitchedOn(), buildedStorage.get(buildedStorage.size()-1).getSwitchedOn());
            Assert.assertEquals("Capacity не совпадает с ожидаемым", testStorage.getCapacity(), buildedStorage.get(buildedStorage.size()-1).getCapacity());
            Assert.assertEquals("CapacityUsed не совпадает с ожидаемым", testStorage.getCapacityUsed(), buildedStorage.get(buildedStorage.size()-1).getCapacityUsed());
            Assert.assertEquals("mountPoint не совпадает с ожидаемым", testStorage.getMountPoint(), buildedStorage.get(buildedStorage.size()-1).getMountPoint());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void createResourceWithInvalidCapacity() {
        testServiceMessage = generateServiceMessage("Хранилище 2", Boolean.TRUE, 0.0, 3 * Math.pow(10, 12), "/home/");
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createResourceWithInvalidCapacityUsed() {
        testServiceMessage = generateServiceMessage("Хранилище 3", Boolean.TRUE, 5 * Math.pow(10, 12), -10.0, "/home/");
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createResourceWithCapacityUsedBiggerThenCapacity() {
        testServiceMessage = generateServiceMessage("Хранилище 4", Boolean.TRUE, 3 * Math.pow(10, 12), 5 * Math.pow(10, 12), "/home/");
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithInvalidCapacity() {
        testStorage.setCapacity(0.0);
        governor.isValid(testStorage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithInvalidCapacityUsed() {
        testStorage.setCapacityUsed(-10.0);
        governor.isValid(testStorage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithInvalidCapacityUsedBiggerThenCapacity() {
        testStorage.setCapacity(3 * Math.pow(10, 12));
        testStorage.setCapacityUsed(5 * Math.pow(10, 12));
        governor.isValid(testStorage);
    }
}
