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
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
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
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ServerRoleRepository serverRoleRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;

    private ServiceMessage testServiceMessage;
    private ServerRole testServerRole;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  List<ServiceTemplate> serviceTemplates) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceTemplates", serviceTemplates);

        return serviceMessage;
    }

    private ServerRole generateServerRole(String name, Boolean switchedOn,
                                          List<ServiceTemplate> serviceTemplates) {
        ServerRole serverRole = new ServerRole();
        serverRole.setName(name);
        serverRole.setSwitchedOn(switchedOn);
        serverRole.setServiceTemplates(serviceTemplates);

        return serverRole;
    }

    @Before
    public void setUp() {
        // Создать и сохранить сервис темплейт
        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplateRepository.save(configTemplate);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.addConfigTemplate(configTemplate);
        serviceTemplateRepository.save(serviceTemplate);

        // Создать сервер роль и сервисное сообщение
        String name = "Серверная роль 1";
        Boolean switchedOn = Boolean.TRUE;
        List<ServiceTemplate> serviceTemplates = new ArrayList<>();
        serviceTemplates.add(serviceTemplate);
        this.testServerRole = generateServerRole(name,switchedOn,serviceTemplates);
        this.testServiceMessage = generateServiceMessage(name,switchedOn,serviceTemplates);
    }

    @Test
    public void create() {
        try {
            ServerRole createdRole = (ServerRole) governor.createResource(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServerRole.getName(), createdRole.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServerRole.getSwitchedOn(), createdRole.getSwitchedOn());
            Assert.assertTrue(testServerRole.getServiceTemplates().equals(createdRole.getServiceTemplates()));
            Assert.assertTrue(testServerRole.getServiceTemplateIds().containsAll(createdRole.getServiceTemplateIds()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        serverRoleRepository.save(testServerRole);
        try {
            ServerRole buildedServerRole = (ServerRole) governor.build(testServerRole.getId());
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServerRole.getName(), buildedServerRole.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServerRole.getSwitchedOn(), buildedServerRole.getSwitchedOn());
            Assert.assertTrue(testServerRole.getServiceTemplates().equals(buildedServerRole.getServiceTemplates()));
            Assert.assertTrue(testServerRole.getServiceTemplateIds().containsAll(buildedServerRole.getServiceTemplateIds()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        serverRoleRepository.save(testServerRole);
        List<ServerRole> buildedServerRoles = governor.build(null, null);
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServerRole.getName(), buildedServerRoles.get(buildedServerRoles.size()-1).getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServerRole.getSwitchedOn(), buildedServerRoles.get(buildedServerRoles.size()-1).getSwitchedOn());
            Assert.assertTrue(testServerRole.getServiceTemplates().equals(buildedServerRoles.get(buildedServerRoles.size() - 1).getServiceTemplates()));
            Assert.assertTrue(testServerRole.getServiceTemplateIds().containsAll(buildedServerRoles.get(buildedServerRoles.size()-1).getServiceTemplateIds()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithUnknownServiceTemplate() throws ParameterValidateException {
        List<String> unknownServiceTemplates = new ArrayList<>();
        unknownServiceTemplates.add(ObjectId.get().toString());
        testServiceMessage.addParam("serviceTemplates", unknownServiceTemplates);
        governor.createResource(testServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithEmptyServiceTemplate() throws ParameterValidateException {
        List<ServiceTemplate> emptyServiceTemplates = new ArrayList<>();
        testServerRole.setServiceTemplates(emptyServiceTemplates);
        governor.isValid(testServerRole);
    }
}
