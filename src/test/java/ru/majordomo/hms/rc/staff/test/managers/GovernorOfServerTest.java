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
import ru.majordomo.hms.rc.staff.event.server.listener.ServerMongoEventListener;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.ConfigOfGovernors;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServletContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ValidationConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                RepositoriesConfig.class,
                ConfigOfGovernors.class,
                EmbeddedServletContainerConfig.class,
                ValidationConfig.class,
                ServerMongoEventListener.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.active.mail-storage.active-storage-mountpoint=/homebig"
        }
)
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

    private ServiceMessage generateServiceMessage(
            String name, Boolean switchedOn,
            List<Service> services, List<ServerRole> serverRoles, List<Storage> storages
    ) {
        List<String> serviceIds = services.stream().map(Resource::getId).collect(Collectors.toList());
        List<String> serverRoleIds = serverRoles.stream().map(Resource::getId).collect(Collectors.toList());
        List<String> storageIds = storages.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceIds", serviceIds);
        serviceMessage.addParam("serverRoleIds", serverRoleIds);
        serviceMessage.addParam("storageIds", storageIds);

        return serviceMessage;
    }

    private Server generateServer(
            String serverId, String name, Boolean switchedOn,
            List<Service> services, List<ServerRole> serverRoles, List<Storage> storages
    ) {
        Server server = new Server();
        server.setId(serverId);
        server.setName(name);
        server.setSwitchedOn(switchedOn);
        server.setServices(services);
        server.setServerRoles(serverRoles);
        server.setStorages(storages);

        return server;
    }

    @Before
    public void setUp() {

        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocketRepository.save(serviceSocket);

        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplateRepository.save(configTemplate);

        ServiceType serviceType = new ServiceType();
        serviceType.setName("DATABASE_MYSQL");
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.addConfigTemplate(configTemplate);
        serviceTemplate.setServiceType(serviceType);
        serviceTemplateRepository.save(serviceTemplate);

        Service service = new Service();
        serviceTypeRepository.save(serviceType);
        service.addServiceSocket(serviceSocket);
        service.setServiceTemplate(serviceTemplate);
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

        List<ServerRole> serverRoles = new ArrayList<>();
        serverRoles.add(serverRole);

        String serverId = ObjectId.get().toString();

        services.forEach(oneService -> {
            oneService.setServerId(serverId);
            serviceRepository.save(oneService);
        });
        storages.forEach(oneStorage -> {
            oneStorage.setServerId(serverId);
            storageRepository.save(oneStorage);
        });

        this.testServer = generateServer(serverId, name, switchedOn, services, serverRoles, storages);
        this.testServiceMessage = generateServiceMessage(name, switchedOn, services, serverRoles, storages);
    }

    @Test
    public void create() {
        try {
            Server createdServer = governor.create(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServer.getName(), createdServer.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServer.getSwitchedOn(), createdServer.getSwitchedOn());
            System.out.println(testServer.getServices() + "\n" + createdServer.getServices());
            //TODO эту хрень теперь нормально не протестить, потому-что мы не знаем id сервисов и сервера и они не совпадают)
//            Assert.assertTrue(testServer.getServices().equals(createdServer.getServices()));
            Assert.assertTrue(testServer.getServiceIds().containsAll(createdServer.getServiceIds()));
            //TODO эту хрень теперь нормально не протестить, потому-что мы не знаем id стораджей и сервера и они не совпадают)
//            Assert.assertTrue(testServer.getStorages().equals(createdServer.getStorages()));
            Assert.assertTrue(testServer.getStorageIds().containsAll(createdServer.getStorageIds()));
            Assert.assertTrue(testServer.getServerRoles().equals(createdServer.getServerRoles()));
            Assert.assertTrue(testServer.getServerRoleIds().containsAll(createdServer.getServerRoleIds()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        serverRepository.save(testServer);
        Server buildedServer = governor.build(testServer.getId());
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServer.getName(), buildedServer.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServer.getSwitchedOn(), buildedServer.getSwitchedOn());
            Assert.assertTrue(testServer.getServices().equals(buildedServer.getServices()));
            Assert.assertTrue(testServer.getServiceIds().containsAll(buildedServer.getServiceIds()));
            Assert.assertTrue(testServer.getStorages().equals(buildedServer.getStorages()));
            Assert.assertTrue(testServer.getStorageIds().containsAll(buildedServer.getStorageIds()));
            Assert.assertTrue(testServer.getServerRoles().equals(buildedServer.getServerRoles()));
            Assert.assertTrue(testServer.getServerRoleIds().containsAll(buildedServer.getServerRoleIds()));
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
            Assert.assertTrue(testServer.getServerRoleIds().containsAll(buildedServers.get(buildedServers.size()-1).getServerRoleIds()));
            Assert.assertTrue(testServer.getServerRoles().equals(buildedServers.get(buildedServers.size()-1).getServerRoles()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildMailStorageServerByActiveStorage() throws Exception {
        ServerRole serverRole = new ServerRole();
        serverRole.setName("mail-storage");

        serverRoleRepository.save(serverRole);

        testServer.addServerRole(serverRole);
        serverRepository.save(testServer);

        Storage storage = new Storage();
        storage.setId(ObjectId.get().toString());
        storage.setMountPoint("/homebig");
        storage.setServerId(testServer.getId());
        storageRepository.save(storage);

        testServer.addStorage(storage);

        storageRepository.save(storage);

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("server-id", testServer.getId());
        keyValue.put("active-storage", "true");
        Storage buildedStorage = governor.build(keyValue).getActiveMailboxStorage();
        Assert.assertNotNull(buildedStorage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createWithUnknownService() {
        List<String> unknownService = new ArrayList<>();
        unknownService.add(ObjectId.get().toString());
        testServiceMessage.addParam("serviceIds", unknownService);
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithEmptyService() {
        List<Service> emptyService = new ArrayList<>();
        testServer.setServices(emptyService);
        governor.isValid(testServer);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createWithUnknownStorage() {
        List<String> unknownStorage = new ArrayList<>();
        unknownStorage.add(ObjectId.get().toString());
        testServiceMessage.addParam("storageIds", unknownStorage);
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithEmptyStorage() {
        List<Storage> emptyStorage = new ArrayList<>();
        testServer.setStorages(emptyStorage);
        governor.isValid(testServer);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createWithUnknownServerRole() {
        List<String> unknownServerRoles = new ArrayList<>();
        unknownServerRoles.add(ObjectId.get().toString());
        testServiceMessage.addParam("serverRoleIds", unknownServerRoles);
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithEmptyServerRoles() {
        List<ServerRole> emptyserverRoles = new ArrayList<>();
        testServer.setServerRoles(emptyserverRoles);
        governor.isValid(testServer);
    }
}
