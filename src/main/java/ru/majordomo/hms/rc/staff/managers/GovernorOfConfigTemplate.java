package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfConfigTemplate extends LordOfResources {
    private static final Logger logger = LoggerFactory.getLogger(GovernorOfConfigTemplate.class);

    private Cleaner cleaner;
    private ConfigTemplateRepository configTemplateRepository;
    private GovernorOfServiceTemplate governorOfServiceTemplate;

    @Autowired
    public void setConfigTemplateRepository(ConfigTemplateRepository configTemplateRepository) {
        this.configTemplateRepository = configTemplateRepository;
    }

    @Autowired
    public void setGovernorOfServiceTemplate(GovernorOfServiceTemplate governorOfServiceTemplate) {
        this.governorOfServiceTemplate = governorOfServiceTemplate;
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
    public Resource build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<ConfigTemplate> buildAll(Map<String, String> keyValue) {

        List<ConfigTemplate> buildedConfigTemplates = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (ConfigTemplate configTemplate : configTemplateRepository.findByName(keyValue.get("name"))) {
                buildedConfigTemplates.add((ConfigTemplate) build(configTemplate.getId()));
            }
        } else {
            for (ConfigTemplate configTemplate : configTemplateRepository.findAll()) {
                buildedConfigTemplates.add((ConfigTemplate) build(configTemplate.getId()));
            }
        }

        return buildedConfigTemplates;
    }

    @Override
    public List<ConfigTemplate> buildAll() {
        return configTemplateRepository.findAll();
    }

    @Override
    public void save(Resource resource) {
        configTemplateRepository.save((ConfigTemplate) resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<ServiceTemplate> serviceTemplates = governorOfServiceTemplate.buildAll();
        for (ServiceTemplate serviceTemplate : serviceTemplates) {
            if (serviceTemplate.getConfigTemplateIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла ServiceTemplate с ID " + serviceTemplate.getId() + ", именуемый " + serviceTemplate.getName() + ", так вот в нём имеется удаляемый ConfigTemplate. Прикинь.");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        configTemplateRepository.delete(resourceId);
    }

}
