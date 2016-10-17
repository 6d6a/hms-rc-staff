package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServerServicesConfig;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class,
        EmbeddedServltetContainerConfig.class, ServerServicesConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GovernorOfServerTest {
    @Autowired
    private GovernorOfServer governor;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private ServerRoleRepository serverRoleRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private StorageRepository storageRepository;

    private ServiceMessage testServiceMessage;
    private Server testServer;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  List<String> serviceIds, String serverRoleId, List<String> storageIds) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceIds", serviceIds);
        serviceMessage.addParam("serverRoleId", serverRoleId);
        serviceMessage.addParam("storageIds", storageIds);

        return serviceMessage;
    }

    private Server generateServer(String name, Boolean switchedOn,
                                          List<Service> services, ServerRole serverRole, List<Storage> storages) {
        Server server = new Server();
        server.setName(name);
        server.setSwitchedOn(switchedOn);
        server.setServices(services);
        server.setServerRole(serverRole);
        server.setStorages(storages);

        return server;
    }

    @Before
    public void setUp() {
        ServerRole serverRole = new ServerRole();
        serverRoleRepository.save(serverRole);

        Service service = new Service();
        serviceRepository.save(service);

        Storage storage = new Storage();
        storageRepository.save(storage);

        // Создать сервер и сервисное сообщение
        String name = "Сервер 1";
        Boolean switchedOn = Boolean.TRUE;

        List<Service> services = new ArrayList<>();
        List<String> serviceIds = new ArrayList<>();
        services.add(service);
        serviceIds.add(service.getId());

        List<Storage> storages = new ArrayList<>();
        List<String> storageIds = new ArrayList<>();
        storages.add(storage);
        storageIds.add(storage.getId());

        this.testServer = generateServer(name, switchedOn, services, serverRole, storages);
        this.testServiceMessage = generateServiceMessage(name, switchedOn, serviceIds, serverRole.getId(), storageIds);
    }

    @Test
    public void create() {
        try {
            Server createdServer = (Server) governor.createResource(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServer.getName(), createdServer.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServer.getSwitchedOn(), createdServer.getSwitchedOn());
            Assert.assertTrue(testServer.getServices().size() == createdServer.getServices().size());
            Assert.assertTrue(testServer.getServiceIds().containsAll(createdServer.getServiceIds()));
            Assert.assertTrue(testServer.getStorages().size() == createdServer.getStorages().size());
            Assert.assertTrue(testServer.getStorageIds().containsAll(createdServer.getStorageIds()));
            Assert.assertTrue(testServer.getServerRoleId().equals(createdServer.getServerRoleId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownService() throws ParameterValidateException {
        List<String> unknownService = new ArrayList<>();
        unknownService.add(ObjectId.get().toString());
        testServiceMessage.addParam("serviceIds", unknownService);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithEmptyService() throws ParameterValidateException {
        List<Service> emptyService = new ArrayList<>();
        testServer.setServices(emptyService);
        governor.isValid(testServer);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownStorage() throws ParameterValidateException {
        List<String> unknownStorage = new ArrayList<>();
        unknownStorage.add(ObjectId.get().toString());
        testServiceMessage.addParam("storageIds", unknownStorage);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithEmptyStorage() throws ParameterValidateException {
        List<Storage> emptyStorage = new ArrayList<>();
        testServer.setStorages(emptyStorage);
        governor.isValid(testServer);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownServerRole() throws ParameterValidateException {
        String serverRoleId = ObjectId.get().toString();
        testServiceMessage.addParam("serverRoleId", serverRoleId);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithEmptyServerRole() throws ParameterValidateException {
        ServerRole emptyServerRole = new ServerRole();
        testServer.setServerRole(emptyServerRole);
        governor.isValid(testServer);
    }
}
