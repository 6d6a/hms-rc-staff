package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceTemplateServicesConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ServiceTemplateServicesConfig.class, RepositoriesConfig.class})
public class GovernorOfServiceTemplateTest {

    @Autowired
    GovernorOfServiceTemplate governorOfServiceTemplate;

    @Autowired
    ConfigTemplateRepository configTemplateRepository;

    @Autowired
    ServiceTemplateRepository serviceTemplateRepository;

    private List<ConfigTemplate> configTemplates = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            String configTemplateId = ObjectId.get().toString();
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setId(configTemplateId);
            configTemplateRepository.save(configTemplate);
            configTemplates.add(configTemplate);
        }
    }

    @Test
    public void create() {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.addParam("configTemplates", configTemplates);
        serviceMessage.addParam("name", "Тестовый service template");
        serviceMessage.addParam("switchedOn", Boolean.TRUE);

        try {
            ServiceTemplate serviceTemplate = (ServiceTemplate) governorOfServiceTemplate.createResource(serviceMessage);
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", serviceTemplate.getName());
            Assert.assertTrue("configTemplates указаны неверно", configTemplates.equals(serviceTemplate.getConfigTemplates()));
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, serviceTemplate.getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void build() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setConfigTemplates(configTemplates);
        serviceTemplate.setName("Тестовый service template");
        serviceTemplate.setSwitchedOn(Boolean.TRUE);
        serviceTemplateRepository.save(serviceTemplate);
        ServiceTemplate buildedServiceTemplate = (ServiceTemplate) governorOfServiceTemplate.build(serviceTemplate.getId());
        try {
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", buildedServiceTemplate.getName());
            Assert.assertTrue("configTemplates указаны неверно", configTemplates.equals(buildedServiceTemplate.getConfigTemplates()));
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, buildedServiceTemplate.getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void buildAll() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setConfigTemplates(configTemplates);
        serviceTemplate.setName("Тестовый service template");
        serviceTemplate.setSwitchedOn(Boolean.TRUE);
        serviceTemplateRepository.save(serviceTemplate);
        List<ServiceTemplate> buildedServiceTemplates = governorOfServiceTemplate.build();
        try {
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", buildedServiceTemplates.get(buildedServiceTemplates.size()-1).getName());
            Assert.assertTrue("configTemplates указаны неверно", configTemplates.equals(buildedServiceTemplates.get(buildedServiceTemplates.size()-1).getConfigTemplates()));
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, buildedServiceTemplates.get(buildedServiceTemplates.size()-1).getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }
}
