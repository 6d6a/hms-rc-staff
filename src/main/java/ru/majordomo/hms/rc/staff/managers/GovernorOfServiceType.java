package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceType;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceTypeChecks;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@Service
public class GovernorOfServiceType extends LordOfResources<ServiceType> {
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private Validator validator;

    @Autowired
    public void setRepository(ServiceTypeRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServiceTemplate(GovernorOfServiceTemplate governorOfServiceTemplate) {
        this.governorOfServiceTemplate = governorOfServiceTemplate;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ServiceType buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        throw new NotImplementedException("Build from AMQP service message not implemented");
    }

    public ServiceType build(String serviceTypeName) throws ResourceNotFoundException {
        ServiceType serviceType = repository.findOneByName(serviceTypeName.toUpperCase());
        if (serviceType == null) {
            throw new ResourceNotFoundException("ServiceType ?? ????????????:" + serviceTypeName + " ???? ????????????");
        }
        return serviceType;
    }

    @Override
    public ServiceType build(Map<String, String> keyValue) throws ResourceNotFoundException {
        throw new NotImplementedException();
    }

    public void isValid(ServiceType serviceType) {
        Set<ConstraintViolation<ServiceType>> constraintViolations = validator.validate(serviceType, ServiceTypeChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("serviceType: " + serviceType + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public void save(ServiceType serviceType) {
        serviceType.setName(serviceType.getName().toUpperCase());
        isValid(serviceType);
        repository.save(serviceType);
    }

    public void preDelete(String name) {
        List<ServiceTemplate> templates = governorOfServiceTemplate.buildAll();
        for (ServiceTemplate template : templates) {
            if (template.getServiceTypeName().equals(name)) {
                throw new ParameterValidateException("?? ?????????? ServiceTemplate ?? ID "
                        + template.getId() + ", ?????????????????? " + template.getName()
                        + ", ?????? ?????? ?? ?????? ?????????????? ?????????????????? ServiceType.");
            }
        }
    }

    public void delete(String name) {
        preDelete(name);
        repository.deleteByName(name);
    }
}
