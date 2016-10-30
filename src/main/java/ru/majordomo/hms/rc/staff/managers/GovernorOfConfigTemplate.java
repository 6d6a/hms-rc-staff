package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@Component
public class GovernorOfConfigTemplate extends LordOfResources {
    private static final Logger logger = LoggerFactory.getLogger(GovernorOfConfigTemplate.class);

    private Cleaner cleaner;
    private ConfigTemplateRepository configTemplateRepository;

    @Autowired
    public void setConfigTemplateRepository(ConfigTemplateRepository configTemplateRepository) {
        this.configTemplateRepository = configTemplateRepository;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + " ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";

        ConfigTemplate configTemplate = new ConfigTemplate();
        LordOfResources.setResourceParams(configTemplate, serviceMessage, cleaner);
        String fileLink = cleaner.cleanString((String)serviceMessage.getParam("fileLink"));
        configTemplate.setFileLink(fileLink);
        isValid(configTemplate);
        save(configTemplate);
        return configTemplate;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ConfigTemplate configTemplate = (ConfigTemplate) resource;
        String fileLink = configTemplate.getFileLink();
        if (fileLink.equals("")) {
            throw new ParameterValidateException("Адрес не может быть пустым");
        }
        try {
            URL url = new URL(fileLink);
        } catch (MalformedURLException e) {
            throw new ParameterValidateException("Параметр fileLink содержит некорретный URL:'"
                                                    + fileLink + "'");
        }
    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        ConfigTemplate configTemplate = configTemplateRepository.findOne(resourceId);
        if (configTemplate == null) {
            throw new ResourceNotFoundException("ConfigTemplate с ID:" + resourceId + " не найден");
        }
        return configTemplate;
    }

    @Override
    public List<ConfigTemplate> buildAll(String key) {
        switch (key) {
            case "": {
                return configTemplateRepository.findAll();
            }
            default: {
                return configTemplateRepository.findByName(key);
            }
        }
    }

    @Override
    public void save(Resource resource) {
        configTemplateRepository.save((ConfigTemplate) resource);
    }

    @Override
    public void delete(String resourceId) {
        configTemplateRepository.delete(resourceId);
    }

}
