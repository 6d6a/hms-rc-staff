package ru.majordomo.hms.rc.staff.managers;

import com.sun.org.apache.regexp.internal.RE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfServiceTemplate extends LordOfResources{
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

        String serviceTemplateName = cleaner.cleanString((String) serviceMessage.getParam("name"));
        if (!serviceTemplateName.equals("")) {
            serviceTemplate.setName(serviceTemplateName);
        } else {
            throw new ParameterValidateException(loggerPrefix + " name не может быть пустым");
        }

        List<String> configTemplateIdsList = cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("configTemplateList"));
        for (String id: configTemplateIdsList) {
            ConfigTemplate configTemplate = configTemplateRepository.findOne(id);
            if (configTemplate == null) {
                throw new ParameterValidateException(loggerPrefix + " configTemplate с ID:" + id + " не найден");
            } else {
                serviceTemplate.addConfigTemplate(configTemplate);
            }
        }

        return serviceTemplate;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ServiceTemplate template = (ServiceTemplate) resource;

    }
}
