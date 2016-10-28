package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
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

    @Autowired
    GovernorOfConfigTemplate governorOfConfigTemplate;

    private static final Logger logger = LoggerFactory.getLogger(GovernorOfServiceTemplate.class);

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + " ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        LordOfResources.setResourceParams(serviceTemplate, serviceMessage, cleaner);

        List<ConfigTemplate> configTemplates = (List<ConfigTemplate>)serviceMessage.getParam("configTemplates");
        serviceTemplate.setConfigTemplates(configTemplates);
        isValid(serviceTemplate);
        save(serviceTemplate);

        return serviceTemplate;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ServiceTemplate template = (ServiceTemplate) resource;
        List<String> configTemplateIds = template.getConfigTemplateIds();
        List<ConfigTemplate> configTemplates = (List<ConfigTemplate>) configTemplateRepository.findAll(configTemplateIds);
        if (configTemplateIds.size() != configTemplates.size()) {
            throw new ParameterValidateException("Передан некорретный список configTemplates");
        }
    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        ServiceTemplate serviceTemplate = serviceTemplateRepository.findOne(resourceId);
        if (serviceTemplate == null) {
            throw new ResourceNotFoundException("ServiceTemplate с ID:" + resourceId + " не найден");
        }
        for (String configTemplateId: serviceTemplate.getConfigTemplateIds()) {
            ConfigTemplate configTemplate = (ConfigTemplate) governorOfConfigTemplate.build(configTemplateId);
            serviceTemplate.addConfigTemplate(configTemplate);
        }
        return serviceTemplate;
    }

    @Override
    public List<ServiceTemplate> build() {
        List<ServiceTemplate> buildedServiceTemplates = new ArrayList<>();
        for (ServiceTemplate serviceTemplate : serviceTemplateRepository.findAll()) {
            buildedServiceTemplates.add((ServiceTemplate) build(serviceTemplate.getId()));
        }
        return buildedServiceTemplates;
    }

    @Override
    public void save(Resource resource) {
        serviceTemplateRepository.save((ServiceTemplate) resource);
    }

    @Override
    public void delete(String resourceId) {
        serviceTemplateRepository.delete(resourceId);
    }

}
