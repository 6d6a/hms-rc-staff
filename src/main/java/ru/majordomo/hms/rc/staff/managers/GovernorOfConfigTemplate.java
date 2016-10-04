package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@Component
public class GovernorOfConfigTemplate extends LordOfResources {
    private static final Logger logger = LoggerFactory.getLogger(GovernorOfConfigTemplate.class);

    @Autowired
    Cleaner cleaner;

    @Autowired
    ConfigTemplateRepository configTemplateRepository;

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + " ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";

        ConfigTemplate configTemplate = new ConfigTemplate();
        String fileName = cleaner.cleanString((String)serviceMessage.getParam("fileName"));
        String fileLink = cleaner.cleanString((String)serviceMessage.getParam("fileLink"));

        isValid(configTemplate);
        configTemplateRepository.save(configTemplate);
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
            throw new ParameterValidateException("Параметр fileLink содержит некорретный URL");
        }
    }


}
