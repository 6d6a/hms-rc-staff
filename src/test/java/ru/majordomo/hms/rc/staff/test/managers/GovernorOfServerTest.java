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
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.resources.*;
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
    @Autowired
    private ServiceSocketRepository serviceSocketRepository;
    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private ServiceMessage testServiceMessage;
    private Server testServer;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  List<Service> services, ServerRole serverRole, List<Storage> storages) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("services", services);
        serviceMessage.addParam("serverRole", serverRole);
        serviceMessage.addParam("storages", storages);

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

        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocketRepository.save(serviceSocket);

        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplateRepository.save(configTemplate);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.addConfigTemplate(configTemplate);
        serviceTemplateRepository.save(serviceTemplate);

        Service service = new Service();
        ServiceType serviceType = new ServiceType();
        serviceType.setName("DATABASE_MYSQL");
        serviceTypeRepository.save(serviceType);
        service.addServiceSocket(serviceSocket);
        service.setServiceTemplate(serviceTemplate);
        service.setServiceType(serviceType);
        serviceRepository.save(service);

        ServerRole serverRole = new ServerRole();
        serverRole.addServiceTemplate(serviceTemplate);
        serverRoleRepository.save(serverRole);

        Storage storage = new Storage();
        storageRepository.save(storage);

        // Создать сервер и сервисное сообщение
        String name = "Сервер 1";
        Boolean switchedOn = Boolean.TRUE;

        List<Service> services = new ArrayList<>();
        services.add(service);

        List<Storage> storages = new ArrayList<>();
        storages.add(storage);

        this.testServer = generateServer(name, switchedOn, services, serverRole, storages);
        this.testServiceMessage = generateServiceMessage(name, switchedOn, services, serverRole, storages);
    }

    @Test
    public void create() {
        try {
            Server createdServer = (Server) governor.createResource(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServer.getName(), createdServer.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServer.getSwitchedOn(), createdServer.getSwitchedOn());
            Assert.assertTrue(testServer.getServices().equals(createdServer.getServices()));
            Assert.assertTrue(testServer.getServiceIds().containsAll(createdServer.getServiceIds()));
            Assert.assertTrue(testServer.getStorages().equals(createdServer.getStorages()));
            Assert.assertTrue(testServer.getStorageIds().containsAll(createdServer.getStorageIds()));
            Assert.assertTrue(testServer.getServerRole().equals(createdServer.getServerRole()));
            Assert.assertTrue(testServer.getServerRoleId().equals(createdServer.getServerRoleId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        serverRepository.save(testServer);
        Server buildedServer = (Server) governor.build(testServer.getId());
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServer.getName(), buildedServer.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServer.getSwitchedOn(), buildedServer.getSwitchedOn());
            Assert.assertTrue(testServer.getServices().equals(buildedServer.getServices()));
            Assert.assertTrue(testServer.getServiceIds().containsAll(buildedServer.getServiceIds()));
            Assert.assertTrue(testServer.getStorages().equals(buildedServer.getStorages()));
            Assert.assertTrue(testServer.getStorageIds().containsAll(buildedServer.getStorageIds()));
            Assert.assertTrue(testServer.getServerRole().equals(buildedServer.getServerRole()));
            Assert.assertTrue(testServer.getServerRoleId().equals(buildedServer.getServerRoleId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        serverRepository.save(testServer);
        List<Server> buildedServers = governor.buildAll();
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServer.getName(), buildedServers.get(buildedServers.size()-1).getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServer.getSwitchedOn(), buildedServers.get(buildedServers.size()-1).getSwitchedOn());
            Assert.assertTrue(testServer.getServices().equals(buildedServers.get(buildedServers.size()-1).getServices()));
            Assert.assertTrue(testServer.getServiceIds().containsAll(buildedServers.get(buildedServers.size()-1).getServiceIds()));
            Assert.assertTrue(testServer.getStorages().equals(buildedServers.get(buildedServers.size()-1).getStorages()));
            Assert.assertTrue(testServer.getStorageIds().containsAll(buildedServers.get(buildedServers.size()-1).getStorageIds()));
            Assert.assertTrue(testServer.getServerRoleId().equals(buildedServers.get(buildedServers.size()-1).getServerRoleId()));
            Assert.assertTrue(testServer.getServerRole().equals(buildedServers.get(buildedServers.size()-1).getServerRole()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownService() throws ParameterValidateException {
        List<String> unknownService = new ArrayList<>();
        unknownService.add(ObjectId.get().toString());
        testServiceMessage.addParam("services", unknownService);
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
        testServiceMessage.addParam("storages", unknownStorage);
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
        testServiceMessage.addParam("serverRole", serverRoleId);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithEmptyServerRole() throws ParameterValidateException {
        ServerRole emptyServerRole = new ServerRole();
        testServer.setServerRole(emptyServerRole);
        governor.isValid(testServer);
    }
}
