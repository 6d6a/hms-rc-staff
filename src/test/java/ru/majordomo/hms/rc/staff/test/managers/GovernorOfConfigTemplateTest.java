package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.majordomo.hms.rc.staff.test.config.ConfigTemplateServicesConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ConfigTemplateServicesConfig.class)
public class GovernorOfConfigTemplateTest {
    @Autowired
    GovernorOfConfigTemplate governorOfConfigTemplate;

    @Test
    public void createTest() {
        String fileName = "apache.conf";
        String fileLink = "/file/" + ObjectId.get().toString();

        ServiceMessage serviceMessage = buildCreateServiceMessage(fileName, fileLink);
        try {
            ConfigTemplate configTemplate = (ConfigTemplate)governorOfConfigTemplate.createResource(serviceMessage);
            Assert.assertEquals("fileName соответствует ожидаемому", fileName, configTemplate.getName());
            Assert.assertEquals("fileLink соответствует ожидаемому", fileLink, configTemplate.getFileLink());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
        }
    }

    private ServiceMessage buildCreateServiceMessage(String fileName, String fileLink) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.addParam("fileName", fileName);
        serviceMessage.addParam("fileLink", fileLink);

        return serviceMessage;
    }
}
