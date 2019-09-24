package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.event.service.listener.ServiceMongoEventListener;
import ru.majordomo.hms.rc.staff.event.serviceTemplate.listener.ServiceTemplateMongoEventListener;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.repositories.socket.SocketRepository;
import ru.majordomo.hms.rc.staff.repositories.template.TemplateRepository;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.resources.socket.NetworkSocket;
import ru.majordomo.hms.rc.staff.resources.socket.Socket;
import ru.majordomo.hms.rc.staff.resources.template.ApplicationServer;
import ru.majordomo.hms.rc.staff.resources.template.Template;
import ru.majordomo.hms.rc.staff.test.config.ConfigOfGovernors;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServletContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ValidationConfig;

import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                RepositoriesConfig.class,
                ConfigOfGovernors.class,
                EmbeddedServletContainerConfig.class,
                ValidationConfig.class,
                ServiceMongoEventListener.class,
                ServiceTemplateMongoEventListener.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class GovernorOfServiceTest {
    @Autowired
    private GovernorOfService governor;
    @Autowired
    private SocketRepository socketRepository;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;
    @MockBean(name="governorOfServer")
    private GovernorOfServer governorOfServer;

    private ServiceMessage testServiceMessage;
    private Service testService;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                 Template template,
                                                  List<Socket> sockets) {
        List<String> socketIds = sockets.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setAccountId("1");
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("serverId", "1");
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("templateId", template.getId());
        serviceMessage.addParam("socketIds", socketIds);

        return serviceMessage;
    }

    private Service generateService(String name, Boolean switchedOn,
                                    Template template,
                                    List<Socket> sockets) {
        List<String> socketIds = sockets.stream().map(Resource::getId).collect(Collectors.toList());

        Service service = new Service();
        service.setServerId("1");
        service.setName(name);
        service.setSwitchedOn(switchedOn);
        service.setTemplate(template);
        service.setSocketIds(socketIds);

        return service;
    }

    @Before
    public void setUp() {
        Server server = new Server();
        server.setId("1");
        server.setName("server_1");
        server.setSwitchedOn(true);
        server.setServices(Collections.emptyList());
        server.setServerRoles(Collections.emptyList());
        server.setStorages(Collections.emptyList());

        Mockito.when(governorOfServer.build(eq("1")))
                .thenReturn(server);

        NetworkSocket socket = new NetworkSocket();
        socket.setAddress("10.10.10.1");
        socket.setPort(2000);
        socket.setProtocol("http");
        socket.setName(socket.getAddressAsString() + ":" + socket.getPort());
        socketRepository.save(socket);

        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplateRepository.save(configTemplate);

        ApplicationServer applicationServer = new ApplicationServer();
        applicationServer.setName("php");
        applicationServer.addConfigTemplate(configTemplate);
        applicationServer.setAvailableToAccounts(true);
        applicationServer.setVersion("5.6");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);
        applicationServer.setSupervisionType("systemd");
        templateRepository.save(applicationServer);

        // Создать сервис и сервисное сообщение
        String name = "1-php@server_1";
        Boolean switchedOn = Boolean.TRUE;
        List<Socket> sockets = new ArrayList<>();
        sockets.add(socket);
        this.testService = generateService(name, switchedOn, applicationServer, sockets);
        this.testServiceMessage = generateServiceMessage(name, switchedOn, applicationServer, sockets);
    }

    @Test
    public void create() {
        try {
            Service createdService = governor.create(testServiceMessage);
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), createdService.getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), createdService.getSwitchedOn());
            Assert.assertEquals("template не совпадает с ожидаемым", testService.getTemplate().getId(), createdService.getTemplate().getId());
//            Assert.assertTrue(testService.getSocketIds().equals(createdService.getSocketIds()));
//            Assert.assertTrue(testService.getSocketIds().containsAll(createdService.getSocketIds()));
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        serviceRepository.save(testService);
        Service buildedService = governor.build(testService.getId());
        try {
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), buildedService.getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), buildedService.getSwitchedOn());
            Assert.assertEquals("template не совпадает с ожидаемым", testService.getTemplate().getId(), buildedService.getTemplate().getId());
            Assert.assertTrue(testService.getServiceSocketIds().equals(buildedService.getServiceSocketIds()));
            Assert.assertTrue(testService.getServiceSocketIds().containsAll(buildedService.getServiceSocketIds()));
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        serviceRepository.save(testService);
        String emptyString = "";
        List<Service> buildedServices = governor.buildAll();
        try {
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), buildedServices.get(buildedServices.size() - 1).getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), buildedServices.get(buildedServices.size() - 1).getSwitchedOn());
            Assert.assertEquals("template не совпадает с ожидаемым", testService.getTemplate().getId(), buildedServices.get(buildedServices.size() - 1).getTemplate().getId());
            Assert.assertTrue(testService.getSocketIds().equals(buildedServices.get(buildedServices.size() - 1).getSocketIds()));
            Assert.assertTrue(testService.getSocketIds().containsAll(buildedServices.get(buildedServices.size() - 1).getSocketIds()));
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

//    @Test(expected = ConstraintViolationException.class)
//    public void createWithUnknownSocket() {
//        List<String> unknownSocketList = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            unknownSocketList.add(ObjectId.get().toString());
//        }
//        testServiceMessage.addParam("socketIds", unknownSocketList);
//        governor.create(testServiceMessage);
//    }

    @Test(expected = ResourceNotFoundException.class)
    public void createWithUnknownTemplate() {
        testServiceMessage.addParam("templateId", ObjectId.get().toString());
        governor.create(testServiceMessage);
    }

    @After
    public void deleteAll() {
        socketRepository.deleteAll();
        configTemplateRepository.deleteAll();
        templateRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        serviceRepository.deleteAll();
    }
}
