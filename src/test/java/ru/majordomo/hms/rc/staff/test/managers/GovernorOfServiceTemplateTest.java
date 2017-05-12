package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.event.serviceTemplate.listener.ServiceTemplateMongoEventListener;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.ServiceType;
import ru.majordomo.hms.rc.staff.test.config.ConfigOfGovernors;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.ValidationConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        RepositoriesConfig.class,
        ConfigOfGovernors.class,
        ValidationConfig.class,
        ServiceTemplateMongoEventListener.class
})
public class GovernorOfServiceTemplateTest {

    @Autowired
    private GovernorOfServiceTemplate governorOfServiceTemplate;

    @Autowired
    private ConfigTemplateRepository configTemplateRepository;

    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private List<ConfigTemplate> configTemplates = new ArrayList<>();
    private List<ServiceType> serviceTypes = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            String configTemplateId = ObjectId.get().toString();

            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setId(configTemplateId);
            configTemplate.setName("configTemplate_" + i);
            configTemplateRepository.save(configTemplate);
            configTemplates.add(configTemplate);
        }

        ServiceType serviceType = new ServiceType();
        serviceType.setName("MAILBOX_TYPE");
        serviceTypeRepository.save(serviceType);
        serviceTypes.add(serviceType);
    }

    @Test
    public void create() {
        List<String> configTemplateIds = configTemplates.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.addParam("configTemplateIds", configTemplateIds);
        serviceMessage.addParam("serviceTypeName", serviceTypes.get(0).getName());
        serviceMessage.addParam("name", "Тестовый service template");
        serviceMessage.addParam("switchedOn", Boolean.TRUE);

        try {
            ServiceTemplate serviceTemplate = governorOfServiceTemplate.createResource(serviceMessage);
            System.out.println("[serviceTemplate] " + serviceTemplate);
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", serviceTemplate.getName());
            Assert.assertTrue("configTemplates указаны неверно", configTemplates.equals(serviceTemplate.getConfigTemplates()));
            Assert.assertTrue("configTemplateIds указаны неверно", configTemplateIds.equals(serviceTemplate.getConfigTemplateIds()));
            Assert.assertTrue("serviceType указан неверно", serviceTypes.get(0).equals(serviceTemplate.getServiceType()));
            Assert.assertTrue("serviceTypeName указан неверно", serviceTypes.get(0).getName().equals(serviceTemplate.getServiceType().getName()));
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, serviceTemplate.getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void build() {
        List<String> configTemplateIds = configTemplates.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceType serviceType = new ServiceType();
        serviceType.setName("DATABASE_MYSQL");
        serviceTypeRepository.save(serviceType);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
//        serviceTemplate.setConfigTemplates(configTemplates);
        serviceTemplate.setConfigTemplateIds(configTemplateIds);
        serviceTemplate.setName("Тестовый service template");
        serviceTemplate.setSwitchedOn(Boolean.TRUE);
//        serviceTemplate.setServiceType(serviceType);
        serviceTemplate.setServiceTypeName(serviceType.getName());
        serviceTemplateRepository.save(serviceTemplate);

        ServiceTemplate buildedServiceTemplate = governorOfServiceTemplate.build(serviceTemplate.getId());

        try {
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", buildedServiceTemplate.getName());
            Assert.assertTrue("configTemplates указаны неверно", configTemplates.equals(buildedServiceTemplate.getConfigTemplates()));
            Assert.assertTrue("configTemplateIds указаны неверно", configTemplateIds.equals(serviceTemplate.getConfigTemplateIds()));
            Assert.assertTrue("serviceType указан неверно", serviceType.equals(serviceTemplate.getServiceType()));
            Assert.assertTrue("serviceTypeName указан неверно", serviceType.getName().equals(serviceTemplate.getServiceType().getName()));
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, buildedServiceTemplate.getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void buildAll() {
        List<String> configTemplateIds = configTemplates.stream().map(Resource::getId).collect(Collectors.toList());

        ServiceType serviceType = new ServiceType();
        serviceType.setName("DATABASE_MYSQL");
        serviceTypeRepository.save(serviceType);

        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setConfigTemplateIds(configTemplateIds);
//        serviceTemplate.setConfigTemplates(configTemplates);
        serviceTemplate.setName("Тестовый service template");
        serviceTemplate.setSwitchedOn(Boolean.TRUE);
        serviceTemplate.setServiceTypeName(serviceType.getName());
//        serviceTemplate.setServiceType(serviceType);
        serviceTemplateRepository.save(serviceTemplate);

        List<ServiceTemplate> buildedServiceTemplates = governorOfServiceTemplate.buildAll();

        try {
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", buildedServiceTemplates.get(buildedServiceTemplates.size()-1).getName());
            Assert.assertTrue("configTemplates указаны неверно", configTemplates.equals(buildedServiceTemplates.get(buildedServiceTemplates.size()-1).getConfigTemplates()));
            Assert.assertTrue("configTemplateIds указаны неверно", configTemplateIds.equals(serviceTemplate.getConfigTemplateIds()));
            Assert.assertTrue("serviceType указан неверно", serviceType.equals(serviceTemplate.getServiceType()));
            Assert.assertTrue("serviceTypeName указан неверно", serviceType.getName().equals(serviceTemplate.getServiceType().getName()));
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, buildedServiceTemplates.get(buildedServiceTemplates.size()-1).getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }

    @After
    public void deleteAll() {
        configTemplateRepository.deleteAll();
        serviceTemplateRepository.deleteAll();
        serviceTypeRepository.deleteAll();
    }
}
