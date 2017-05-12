package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceTemplateChecks;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@Component
public class GovernorOfServiceTemplate extends LordOfResources<ServiceTemplate> {
    private Cleaner cleaner;
    private ServiceTemplateRepository serviceTemplateRepository;
    private GovernorOfService governorOfService;
    private GovernorOfServerRole governorOfServerRole;
    private Validator validator;

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setServiceTemplateRepository(ServiceTemplateRepository serviceTemplateRepository) {
        this.serviceTemplateRepository = serviceTemplateRepository;
    }

    @Autowired
    public void setGovernorOfService(GovernorOfService governorOfService) {
        this.governorOfService = governorOfService;
    }

    @Autowired
    public void setGovernorOfServerRole(GovernorOfServerRole governorOfServerRole) {
        this.governorOfServerRole = governorOfServerRole;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ServiceTemplate createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        LordOfResources.setResourceParams(serviceTemplate, serviceMessage, cleaner);

        @SuppressWarnings("unchecked") List<String> configTemplateIds = (List<String>) serviceMessage.getParam("configTemplateIds");
        serviceTemplate.setConfigTemplateIds(configTemplateIds);
        serviceTemplate.setServiceTypeName((String) serviceMessage.getParam("serviceTypeName"));
        isValid(serviceTemplate);
        save(serviceTemplate);

        return serviceTemplate;
    }

    @Override
    public void isValid(ServiceTemplate serviceTemplate) throws ParameterValidateException {
        Set<ConstraintViolation<ServiceTemplate>> constraintViolations = validator.validate(serviceTemplate, ServiceTemplateChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("serviceTemplate: " + serviceTemplate + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public ServiceTemplate build(String resourceId) throws ResourceNotFoundException {
        ServiceTemplate serviceTemplate = serviceTemplateRepository.findOne(resourceId);
        if (serviceTemplate == null) {
            throw new ResourceNotFoundException("ServiceTemplate с ID:" + resourceId + " не найден");
        }

        if (serviceTemplate.getServiceTypeName() == null) {
            throw new ParameterValidateException("ServiceTypeName отсутствует");
        }

        return serviceTemplate;
    }

    @Override
    public ServiceTemplate build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<ServiceTemplate> buildAll(Map<String, String> keyValue) {

        List<ServiceTemplate> buildedServiceTemplates = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (ServiceTemplate serviceTemplate : serviceTemplateRepository.findByName(keyValue.get("name"))) {
                buildedServiceTemplates.add(build(serviceTemplate.getId()));
            }
        } else {
            for (ServiceTemplate serviceTemplate : serviceTemplateRepository.findAll()) {
                buildedServiceTemplates.add(build(serviceTemplate.getId()));
            }
        }

        return buildedServiceTemplates;
    }

    @Override
    public List<ServiceTemplate> buildAll() {
        List<ServiceTemplate> buildedServiceTemplates = new ArrayList<>();
        for (ServiceTemplate serviceTemplate : serviceTemplateRepository.findAll()) {
            buildedServiceTemplates.add(build(serviceTemplate.getId()));
        }
        return buildedServiceTemplates;
    }

    @Override
    public void save(ServiceTemplate resource) {
        serviceTemplateRepository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<ServerRole> roles = governorOfServerRole.buildAll();
        for (ServerRole role : roles) {
            if (role.getServiceTemplateIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла ServerRole с ID " + role.getId()
                        + ", именуемый " + role.getName() + ", так вот в нём имеется удаляемый ServiceTemplate. A-ta-ta.");
            }
        }

        List<Service> services = governorOfService.buildAll();
        for (Service service : services) {
            if (service.getServiceTemplateId().equals(resourceId)) {
                throw new ParameterValidateException("Я нашла Service с ID " + service.getId()
                        + ", именуемый " + service.getName()
                        + ", так вот в нём имеется удаляемый ServiceTemplate. What's wrong with this rebatishki?");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        serviceTemplateRepository.delete(resourceId);
    }

}
