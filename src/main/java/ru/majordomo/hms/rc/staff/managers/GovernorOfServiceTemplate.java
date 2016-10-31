package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfServiceTemplate extends LordOfResources {
    private Cleaner cleaner;
    private ServiceTemplateRepository serviceTemplateRepository;
    private GovernorOfConfigTemplate governorOfConfigTemplate;

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setServiceTemplateRepository(ServiceTemplateRepository serviceTemplateRepository) {
        this.serviceTemplateRepository = serviceTemplateRepository;
    }

    @Autowired
    public void setGovernorOfConfigTemplate(GovernorOfConfigTemplate governorOfConfigTemplate) {
        this.governorOfConfigTemplate = governorOfConfigTemplate;
    }

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
        ServiceTemplate serviceTemplate = (ServiceTemplate) resource;

        if (serviceTemplate.getConfigTemplates().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ConfigTemplate");
        }
        if (serviceTemplate.getConfigTemplateIds().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ConfigTemplateId");
        }

        //Валидация ConfigTemplate
        for (ConfigTemplate configTemplateToValidate : serviceTemplate.getConfigTemplates()) {
            ConfigTemplate configTemplateFromRepository = (ConfigTemplate) governorOfConfigTemplate.build(configTemplateToValidate.getId());
            if (configTemplateFromRepository == null) {
                throw new ParameterValidateException("ConfigTemplate с ID: " + configTemplateToValidate.getId() + " не найден");
            }
            if(!configTemplateFromRepository.equals(configTemplateToValidate)) {
                throw new ParameterValidateException("ConfigTemplate с ID: " + configTemplateToValidate.getId() + " задан некорректно");
            }
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
    public List<ServiceTemplate> build(Map<String, String> keyValue) {

        List<ServiceTemplate> buildedServiceTemplates = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (ServiceTemplate serviceTemplate : serviceTemplateRepository.findByName(keyValue.get("name"))) {
                buildedServiceTemplates.add((ServiceTemplate) build(serviceTemplate.getId()));
            }
        } else {
            for (ServiceTemplate serviceTemplate : serviceTemplateRepository.findAll()) {
                buildedServiceTemplates.add((ServiceTemplate) build(serviceTemplate.getId()));
            }
        }

        return buildedServiceTemplates;
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
