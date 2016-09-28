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
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
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
    GovernorOfService governor;
    @Autowired
    ServiceSocketRepository socketRepository;
    @Autowired
    ServiceTemplateRepository templateRepository;

    ServiceMessage testServiceMessage;
    Service testService;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  String serviceTemplateId,
                                                  List<String> serviceSocketIdList) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceTemplate", serviceTemplateId);
        serviceMessage.addParam("serviceSocketList", serviceSocketIdList);

        return serviceMessage;
    }

    private Service generateService(String name, Boolean switchedOn,
                                    ServiceTemplate serviceTemplate,
                                    List<ServiceSocket> serviceSocketList) {
        Service service = new Service();
        service.setName(name);
        service.setSwitchedOn(switchedOn);
        service.setServiceTemplate(serviceTemplate);
        service.setServiceSocketList(serviceSocketList);

        return service;
    }

    @Before
    public void setUp() {
        /* Создать и сохранить сокет
           Т.к. для теста нужен только ID сокета, больше ничего не делаем
         */
        ServiceSocket socket = new ServiceSocket();
        socketRepository.save(socket);

        /* Создать темплейт
           Аналогично - для теста нужен только ID темплейта
         */
        ServiceTemplate template = new ServiceTemplate();
        templateRepository.save(template);

        // Создать сервис и сервисное сообщение
        String name = "Тестовый сервис 1";
        Boolean switchedOn = Boolean.TRUE;
        List<ServiceSocket> socketList = new ArrayList<>();
        List<String> socketIdList = new ArrayList<>();
        socketList.add(socket);
        socketIdList.add(socket.getId());
        this.testService = generateService(name, switchedOn, template, socketList);
        this.testServiceMessage = generateServiceMessage(name, switchedOn, template.getId(), socketIdList);
    }

    @Test
    public void create() {
        try {
            Service createdService = (Service) governor.createResource(testServiceMessage);
            Assert.assertEquals("name не совпадает с ожидаемым", testService.getName(), createdService.getName());
            Assert.assertEquals("switchedOn не совпадает с ожидаемым", testService.getSwitchedOn(), createdService.getSwitchedOn());
            Assert.assertEquals("serviceTemplate не совпадает с ожидаемым", testService.getServiceTemplate().getId(), createdService.getServiceTemplate().getId());
            Assert.assertTrue(testService.getServiceSocketIdList().size() == createdService.getServiceSocketIdList().size());
            Assert.assertTrue(testService.getServiceSocketIdList().containsAll(createdService.getServiceSocketIdList()));
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
        testServiceMessage.addParam("serviceSocketList", unknownSocketList);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownTemplate() throws ParameterValidateException {
        testServiceMessage.addParam("serviceTemplate", ObjectId.get().toString());
        governor.createResource(testServiceMessage);
    }
}
