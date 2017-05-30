package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.event.service.listener.ServiceMongoEventListener;
import ru.majordomo.hms.rc.staff.event.serviceTemplate.listener.ServiceTemplateMongoEventListener;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.test.config.ConfigOfGovernors;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServletContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ValidationConfig;

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
    private ServiceSocketRepository serviceSocketRepository;
    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private ServiceMessage testServiceMessage;
    private Service testService;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  ServiceTemplate serviceTemplate,
                                                  List<ServiceSocket> serviceSockets) {
        List<String> serviceSocketIds = serviceSockets.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceTemplateId", serviceTemplate.getId());
        serviceMessage.addParam("serviceSocketIds", serviceSocketIds);

        return serviceMessage;
    }

    private Service generateService(String name, Boolean switchedOn,
                                    ServiceTemplate serviceTemplate,
                                    List<ServiceSocket> serviceSockets) {
        List<String> serviceSocketIds = serviceSockets.stream().map(Resource::getId).collect(Collectors.toList());

        Service service = new Service();
        service.setName(name);
        service.setSwitchedOn(switchedOn);
        service.setServiceTemplate(serviceTemplate);
        service.setServiceSocketIds(serviceSocketIds);

        return service;
    }

    @Before
    public void setUp() {

        ServiceType serviceType = new ServiceType();
        serviceType.setName("DATABASE_MYSQL");
        serviceTypeRepository.save(serviceType);

        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocketRepository.save(serviceSocket);

        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplateRepository.save(configTemplate);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.addConfigTemplate(configTemplate);
        serviceTemplate.setServiceType(serviceType);
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
            Service createdService = governor.createResource(testServiceMessage);
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
        Service buildedService = governor.build(testService.getId());
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
        List<Service> buildedServices = governor.buildAll();
        try {
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), buildedServices.get(buildedServices.size() - 1).getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), buildedServices.get(buildedServices.size() - 1).getSwitchedOn());
            Assert.assertEquals("serviceTemplate не совпадает с ожидаемым", testService.getServiceTemplate().getId(), buildedServices.get(buildedServices.size() - 1).getServiceTemplate().getId());
            Assert.assertTrue(testService.getServiceSocketIds().equals(buildedServices.get(buildedServices.size() - 1).getServiceSocketIds()));
            Assert.assertTrue(testService.getServiceSocketIds().containsAll(buildedServices.get(buildedServices.size() - 1).getServiceSocketIds()));
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void createWithUnknownSocket() {
        List<String> unknownSocketList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            unknownSocketList.add(ObjectId.get().toString());
        }
        testServiceMessage.addParam("serviceSocketIds", unknownSocketList);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createWithUnknownTemplate() {
        testServiceMessage.addParam("serviceTemplateId", ObjectId.get().toString());
        governor.createResource(testServiceMessage);
    }

    @After
    public void deleteAll() {
        serviceSocketRepository.deleteAll();
        configTemplateRepository.deleteAll();
        serviceTemplateRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        serviceRepository.deleteAll();
    }
}
