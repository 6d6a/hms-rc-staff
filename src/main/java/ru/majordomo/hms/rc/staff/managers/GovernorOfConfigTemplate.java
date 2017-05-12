package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.validation.group.ConfigTemplateChecks;

@Component
public class GovernorOfConfigTemplate extends LordOfResources<ConfigTemplate> {
    private Cleaner cleaner;
    private ConfigTemplateRepository configTemplateRepository;
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private Validator validator;

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

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ConfigTemplate createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        ConfigTemplate configTemplate = new ConfigTemplate();
        LordOfResources.setResourceParams(configTemplate, serviceMessage, cleaner);
        String fileLink = cleaner.cleanString((String)serviceMessage.getParam("fileLink"));
        configTemplate.setFileLink(fileLink);
        isValid(configTemplate);
        save(configTemplate);
        return configTemplate;
    }

    @Override
    public void isValid(ConfigTemplate configTemplate) throws ParameterValidateException {
        Set<ConstraintViolation<ConfigTemplate>> constraintViolations = validator.validate(configTemplate, ConfigTemplateChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("configTemplate: " + configTemplate + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public ConfigTemplate build(String resourceId) throws ResourceNotFoundException {
        ConfigTemplate configTemplate = configTemplateRepository.findOne(resourceId);
        if (configTemplate == null) {
            throw new ResourceNotFoundException("ConfigTemplate с ID:" + resourceId + " не найден");
        }
        return configTemplate;
    }

    @Override
    public ConfigTemplate build(Map<String, String> keyValue) throws NotImplementedException {
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
                buildedConfigTemplates.add(build(configTemplate.getId()));
            }
        } else {
            for (ConfigTemplate configTemplate : configTemplateRepository.findAll()) {
                buildedConfigTemplates.add(build(configTemplate.getId()));
            }
        }

        return buildedConfigTemplates;
    }

    @Override
    public List<ConfigTemplate> buildAll() {
        return configTemplateRepository.findAll();
    }

    @Override
    public void save(ConfigTemplate resource) {
        configTemplateRepository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<ServiceTemplate> serviceTemplates = governorOfServiceTemplate.buildAll();
        for (ServiceTemplate serviceTemplate : serviceTemplates) {
            if (serviceTemplate.getConfigTemplateIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла ServiceTemplate с ID "
                        + serviceTemplate.getId() + ", именуемый " + serviceTemplate.getName()
                        + ", так вот в нём имеется удаляемый ConfigTemplate. Прикинь.");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        configTemplateRepository.delete(resourceId);
    }
}
