package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.test.config.ConfigTemplateServicesConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, ConfigTemplateServicesConfig.class})
public class GovernorOfConfigTemplateTest {
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;

    @Autowired
    private GovernorOfConfigTemplate governorOfConfigTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    @Test
    public void create() {
        String fileName = "apache.conf";
        String fileLink = "http://file-service/file/" + ObjectId.get().toString();
        Boolean switchedOn = Boolean.TRUE;

        ServiceMessage serviceMessage = buildCreateServiceMessage(fileName, switchedOn, fileLink);
        try {
            ConfigTemplate configTemplate = (ConfigTemplate)governorOfConfigTemplate.createResource(serviceMessage);
            Assert.assertEquals("name соответствует ожидаемому", fileName, configTemplate.getName());
            Assert.assertEquals("switchedOn соответствует ожидаемомему", switchedOn, configTemplate.getSwitchedOn());
            Assert.assertEquals("fileLink соответствует ожидаемому", fileLink, configTemplate.getFileLink());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        String fileName = "apache.conf";
        String fileLink = "http://file-service/file/" + ObjectId.get().toString();
        Boolean switchedOn = Boolean.TRUE;
        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplate.setFileLink(fileLink);
        configTemplate.setName(fileName);
        configTemplate.setSwitchedOn(switchedOn);
        configTemplateRepository.save(configTemplate);

        ConfigTemplate buildedConfigTemplate = (ConfigTemplate)governorOfConfigTemplate.build(configTemplate.getId());
        try {
            Assert.assertEquals("name соответствует ожидаемому", fileName, buildedConfigTemplate.getName());
            Assert.assertEquals("switchedOn соответствует ожидаемомему", switchedOn, buildedConfigTemplate.getSwitchedOn());
            Assert.assertEquals("fileLink соответствует ожидаемому", fileLink, buildedConfigTemplate.getFileLink());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        String fileName = "apache.conf";
        String fileLink = "http://file-service/file/" + ObjectId.get().toString();
        Boolean switchedOn = Boolean.TRUE;
        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplate.setFileLink(fileLink);
        configTemplate.setName(fileName);
        configTemplate.setSwitchedOn(switchedOn);
        configTemplateRepository.save(configTemplate);

        List<ConfigTemplate> buildedConfigTemplates = governorOfConfigTemplate.build();
        try {
            Assert.assertEquals("name соответствует ожидаемому", fileName, buildedConfigTemplates.get(buildedConfigTemplates.size()-1).getName());
            Assert.assertEquals("switchedOn соответствует ожидаемомему", switchedOn, buildedConfigTemplates.get(buildedConfigTemplates.size()-1).getSwitchedOn());
            Assert.assertEquals("fileLink соответствует ожидаемому", fileLink, buildedConfigTemplates.get(buildedConfigTemplates.size()-1).getFileLink());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test(expected = ParameterValidateException.class)
    public void createWithBadUrl() throws ParameterValidateException {
        String fileName = "apache.conf";
        String fileLink = "/file/" + ObjectId.get().toString();
        Boolean switchedOn = Boolean.TRUE;

        ServiceMessage serviceMessage = buildCreateServiceMessage(fileName, switchedOn, fileLink);
        try {
            ConfigTemplate configTemplate = (ConfigTemplate)governorOfConfigTemplate.createResource(serviceMessage);
        } catch (NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }
    private ServiceMessage buildCreateServiceMessage(String fileName, Boolean switchedOn, String fileLink) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.addParam("name", fileName);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("fileLink", fileLink);

        return serviceMessage;
    }
}
