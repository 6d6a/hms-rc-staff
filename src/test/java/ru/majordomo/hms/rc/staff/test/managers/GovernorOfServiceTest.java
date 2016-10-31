package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceServicesConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class,
        EmbeddedServltetContainerConfig.class, ServiceServicesConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GovernorOfServiceTest {
    @Autowired
    private GovernorOfService governor;
    @Autowired
    private ServiceSocketRepository serviceSocketRepository;
    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    ServiceMessage testServiceMessage;
    Service testService;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  ServiceTemplate serviceTemplate,
                                                  List<ServiceSocket> serviceSockets) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceTemplate", serviceTemplate);
        serviceMessage.addParam("serviceSockets", serviceSockets);

        return serviceMessage;
    }

    private Service generateService(String name, Boolean switchedOn,
                                    ServiceTemplate serviceTemplate,
                                    List<ServiceSocket> serviceSockets) {
        Service service = new Service();
        service.setName(name);
        service.setSwitchedOn(switchedOn);
        service.setServiceTemplate(serviceTemplate);
        service.setServiceSockets(serviceSockets);

        return service;
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

        // Создать сервис и сервисное сообщение
        String name = "Тестовый сервис 1";
        Boolean switchedOn = Boolean.TRUE;
        List<ServiceSocket> serviceSockets = new ArrayList<>();
        serviceSockets.add(serviceSocket);
        this.testService = generateService(name, switchedOn, serviceTemplate, serviceSockets);
        this.testServiceMessage = generateServiceMessage(name, switchedOn, serviceTemplate, serviceSockets);
    }

    @Test
    public void create() {
        try {
            Service createdService = (Service) governor.createResource(testServiceMessage);
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), createdService.getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), createdService.getSwitchedOn());
            Assert.assertEquals("serviceTemplate не совпадает с ожидаемым", testService.getServiceTemplate().getId(), createdService.getServiceTemplate().getId());
            Assert.assertTrue(testService.getServiceSocketIds().equals(createdService.getServiceSocketIds()));
            Assert.assertTrue(testService.getServiceSocketIds().containsAll(createdService.getServiceSocketIds()));
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        serviceRepository.save(testService);
        Service buildedService = (Service) governor.build(testService.getId());
        try {
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), buildedService.getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), buildedService.getSwitchedOn());
            Assert.assertEquals("serviceTemplate не совпадает с ожидаемым", testService.getServiceTemplate().getId(), buildedService.getServiceTemplate().getId());
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
        List<Service> buildedServices =  governor.build(null, null);
        try {
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), buildedServices.get(buildedServices.size()-1).getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), buildedServices.get(buildedServices.size()-1).getSwitchedOn());
            Assert.assertEquals("serviceTemplate не совпадает с ожидаемым", testService.getServiceTemplate().getId(), buildedServices.get(buildedServices.size()-1).getServiceTemplate().getId());
            Assert.assertTrue(testService.getServiceSocketIds().equals(buildedServices.get(buildedServices.size() - 1).getServiceSocketIds()));
            Assert.assertTrue(testService.getServiceSocketIds().containsAll(buildedServices.get(buildedServices.size()-1).getServiceSocketIds()));
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownSocket() throws ParameterValidateException {
        List<String> unknownSocketList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            unknownSocketList.add(ObjectId.get().toString());
        }
        testServiceMessage.addParam("serviceSockets", unknownSocketList);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownTemplate() throws ParameterValidateException {
        testServiceMessage.addParam("serviceTemplate", ObjectId.get().toString());
        governor.createResource(testServiceMessage);
    }
}
