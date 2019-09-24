package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

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
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private Validator validator;

    @Autowired
    public void setConfigTemplateRepository(ConfigTemplateRepository repository) {
        this.repository = repository;
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
    public ConfigTemplate buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        ConfigTemplate configTemplate = new ConfigTemplate();
        LordOfResources.setResourceParams(configTemplate, serviceMessage, cleaner);
        String fileLink = cleaner.cleanString((String)serviceMessage.getParam("fileLink"));
        configTemplate.setFileLink(fileLink);

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
    public ConfigTemplate build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
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
}
