package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void create() {
        List<String> configTemplateIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String configTemplateId = ObjectId.get().toString();
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setId(configTemplateId);
            configTemplateRepository.save(configTemplate);
            configTemplateIds.add(configTemplateId);
        }
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.addParam("configTemplateList", configTemplateIds);
        serviceMessage.addParam("name", "Тестовый service template");
        serviceMessage.addParam("switchedOn", Boolean.TRUE);

        try {
            ServiceTemplate serviceTemplate = (ServiceTemplate) governorOfServiceTemplate.createResource(serviceMessage);
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", serviceTemplate.getName());
            Assert.assertEquals("Количество config templat'ов не соответствует заданному", 10, serviceTemplate.getConfigTemplates().size());
            Assert.assertEquals("Статус включен/выключен установлен неверно", Boolean.TRUE, serviceTemplate.getSwitchedOn());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }
}
