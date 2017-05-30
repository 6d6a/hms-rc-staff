package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private GovernorOfService governorOfService;
    private GovernorOfServerRole governorOfServerRole;
    private Validator validator;

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setServiceTemplateRepository(ServiceTemplateRepository repository) {
        this.repository = repository;
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
    public ServiceTemplate build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
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
}
