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
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServerRoleServicesConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class,
        EmbeddedServltetContainerConfig.class, ServerRoleServicesConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GovernorOfServerRoleTest {
    @Autowired
    private GovernorOfServerRole governor;
    @Autowired
    private ServiceTemplateRepository templateRepository;
    @Autowired
    private ServerRoleRepository repository;

    private ServiceMessage testServiceMessage;
    private ServerRole testServerRole;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  List<String> serviceTemplateIdList) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceTemplateList", serviceTemplateIdList);

        return serviceMessage;
    }

    private ServerRole generateServerRole(String name, Boolean switchedOn,
                                          List<ServiceTemplate> serviceTemplateList) {
        ServerRole serverRole = new ServerRole();
        serverRole.setName(name);
        serverRole.setSwitchedOn(switchedOn);
        serverRole.setServiceTemplateList(serviceTemplateList);

        return serverRole;
    }

    @Before
    public void setUp() {
        // Создать и сохранить сервис темплейт
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        templateRepository.save(serviceTemplate);

        // Создать сервер роль и сервисное сообщение
        String name = "Серверная роль 1";
        Boolean switchedOn = Boolean.TRUE;
        List<ServiceTemplate> serviceTemplateList = new ArrayList<>();
        List<String> serviceTemplateIdList = new ArrayList<>();
        serviceTemplateList.add(serviceTemplate);
        serviceTemplateIdList.add(serviceTemplate.getId());
        this.testServerRole = generateServerRole(name,switchedOn,serviceTemplateList);
        this.testServiceMessage = generateServiceMessage(name,switchedOn,serviceTemplateIdList);
    }

    @Test
    public void create() {
        try {
            ServerRole createdRole = (ServerRole) governor.createResource(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServerRole.getName(), createdRole.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServerRole.getSwitchedOn(), createdRole.getSwitchedOn());
            Assert.assertTrue(testServerRole.getServiceTemplateList().size() == createdRole.getServiceTemplateList().size());
            Assert.assertTrue(testServerRole.getServiceTemplateIdList().containsAll(createdRole.getServiceTemplateIdList()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownServiceTemplate() throws ParameterValidateException {
        List<String> unknownServiceTemplateList = new ArrayList<>();
        unknownServiceTemplateList.add(ObjectId.get().toString());
        testServiceMessage.addParam("serviceTemplateList", unknownServiceTemplateList);
        governor.createResource(testServiceMessage);
    }
}
