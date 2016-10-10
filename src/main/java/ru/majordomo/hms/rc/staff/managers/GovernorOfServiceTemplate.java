package ru.majordomo.hms.rc.staff.managers;

import com.sun.org.apache.regexp.internal.RE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfServiceTemplate extends LordOfResources {
    @Autowired
    Cleaner cleaner;

    @Autowired
    ServiceTemplateRepository serviceTemplateRepository;

    @Autowired
    ConfigTemplateRepository configTemplateRepository;

    private static final Logger logger = LoggerFactory.getLogger(GovernorOfServiceTemplate.class);

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + " ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        LordOfResources.setResourceParams(serviceTemplate, serviceMessage, cleaner);

        List<String> configTemplateIds = cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("configTemplateList"));
        serviceTemplate.setConfigTemplates((List<ConfigTemplate>) configTemplateRepository.findAll(configTemplateIds));
        isValid(serviceTemplate);
        serviceTemplateRepository.save(serviceTemplate);

        return serviceTemplate;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ServiceTemplate template = (ServiceTemplate) resource;
        List<String> configTemplateIds = template.getConfigTemplateIds();
        List<ConfigTemplate> configTemplates = (List<ConfigTemplate>) configTemplateRepository.findAll(configTemplateIds);
        if (configTemplateIds.size() != configTemplates.size()) {
            throw new ParameterValidateException("Передан некорретный список config templat'ов");
        }
    }
}
