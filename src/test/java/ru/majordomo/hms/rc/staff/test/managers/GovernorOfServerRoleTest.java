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
import ru.majordomo.hms.rc.staff.event.serverRole.listener.ServerRoleMongoEventListener;
import ru.majordomo.hms.rc.staff.event.serviceTemplate.listener.ServiceTemplateMongoEventListener;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceType;
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
                ServiceTemplateMongoEventListener.class,
                ServerRoleMongoEventListener.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class GovernorOfServerRoleTest {
    @Autowired
    private GovernorOfServerRole governor;
    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ServerRoleRepository serverRoleRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private ServiceMessage testServiceMessage;
    private ServerRole testServerRole;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  List<ServiceTemplate> serviceTemplates) {
        List<String> serviceTemplateIds = serviceTemplates.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("serviceTemplateIds", serviceTemplateIds);

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
        // ?????????????? ?? ?????????????????? ???????????? ????????????????
        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplateRepository.save(configTemplate);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.addConfigTemplate(configTemplate);
        ServiceType serviceType = new ServiceType();
        serviceType.setName("DATABASE_MYSQL");
        serviceTypeRepository.save(serviceType);
        serviceTemplate.setServiceTypeName(serviceType.getName());
        serviceTemplateRepository.save(serviceTemplate);

        // ?????????????? ???????????? ???????? ?? ?????????????????? ??????????????????
        String name = "?????????????????? ???????? 1";
        Boolean switchedOn = Boolean.TRUE;
        List<ServiceTemplate> serviceTemplates = new ArrayList<>();
        serviceTemplates.add(serviceTemplate);
        this.testServerRole = generateServerRole(name,switchedOn,serviceTemplates);
        this.testServiceMessage = generateServiceMessage(name,switchedOn,serviceTemplates);
    }

    @Test
    public void create() {
        try {
            ServerRole createdRole = governor.create(testServiceMessage);
            Assert.assertEquals("?????? ???? ?????????????????? ?? ??????????????????", testServerRole.getName(), createdRole.getName());
            Assert.assertEquals("???????????? ??????????????/???????????????? ???? ?????????????????? ?? ??????????????????", testServerRole.getSwitchedOn(), createdRole.getSwitchedOn());
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
            ServerRole buildedServerRole = governor.build(testServerRole.getId());
            Assert.assertEquals("?????? ???? ?????????????????? ?? ??????????????????", testServerRole.getName(), buildedServerRole.getName());
            Assert.assertEquals("???????????? ??????????????/???????????????? ???? ?????????????????? ?? ??????????????????", testServerRole.getSwitchedOn(), buildedServerRole.getSwitchedOn());
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
        List<ServerRole> buildedServerRoles = governor.buildAll();
        try {
            Assert.assertEquals("?????? ???? ?????????????????? ?? ??????????????????", testServerRole.getName(), buildedServerRoles.get(buildedServerRoles.size()-1).getName());
            Assert.assertEquals("???????????? ??????????????/???????????????? ???? ?????????????????? ?? ??????????????????", testServerRole.getSwitchedOn(), buildedServerRoles.get(buildedServerRoles.size()-1).getSwitchedOn());
            Assert.assertTrue(testServerRole.getServiceTemplates().equals(buildedServerRoles.get(buildedServerRoles.size() - 1).getServiceTemplates()));
            Assert.assertTrue(testServerRole.getServiceTemplateIds().containsAll(buildedServerRoles.get(buildedServerRoles.size()-1).getServiceTemplateIds()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void createWithUnknownServiceTemplate() {
        List<String> unknownServiceTemplates = new ArrayList<>();
        unknownServiceTemplates.add(ObjectId.get().toString());
        testServiceMessage.addParam("serviceTemplateIds", unknownServiceTemplates);
        governor.create(testServiceMessage);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateWithEmptyServiceTemplate() {
        List<String> emptyServiceTemplates = new ArrayList<>();
        testServerRole.setServiceTemplateIds(emptyServiceTemplates);
        governor.isValid(testServerRole);
    }

    @After
    public void cleanAll() {
        serverRoleRepository.deleteAll();
        configTemplateRepository.deleteAll();
        serviceTemplateRepository.deleteAll();
    }
}
