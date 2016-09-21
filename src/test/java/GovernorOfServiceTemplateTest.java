import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import config.FongoConfig;
import config.GovernorOfConfigTemplateConfig;
import config.GovernorOfServiceTemplateConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {GovernorOfServiceTemplateConfig.class, FongoConfig.class})
public class GovernorOfServiceTemplateTest {

    @Autowired
    GovernorOfServiceTemplate governorOfServiceTemplate;

    @Autowired
    ConfigTemplateRepository configTemplateRepository;

    @Test
    public void create() {
        List<String> configTemplateIdsList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String configTemplateId = ObjectId.get().toString();
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setId(configTemplateId);
            configTemplateRepository.save(configTemplate);
            configTemplateIdsList.add(configTemplateId);
        }
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.addParam("configTemplateList", configTemplateIdsList);
        serviceMessage.addParam("name", "Тестовый service template");

        try {
            ServiceTemplate serviceTemplate = (ServiceTemplate) governorOfServiceTemplate.createResource(serviceMessage);
            Assert.assertEquals("Имя сервиса установлено неверно", "Тестовый service template", serviceTemplate.getName());
            Assert.assertEquals("Количество config templat'ов не соответствует заданному", 10, serviceTemplate.getConfigTemplateList().size());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }
}
