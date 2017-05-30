package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceChecks;

@Component
public class GovernorOfService extends LordOfResources<Service> {
    private GovernorOfServer governorOfServer;
    private Cleaner cleaner;
    private Validator validator;

    @Autowired
    public void setRepository(ServiceRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServer(GovernorOfServer governorOfServer) {
        this.governorOfServer = governorOfServer;
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
    public Service createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Service service = new Service();
        try {
            LordOfResources.setResourceParams(service, serviceMessage, cleaner);
            String serviceTemplateId = (String) serviceMessage.getParam("serviceTemplateId");
            @SuppressWarnings("unchecked") List<String> serviceSocketIds = (List<String>) serviceMessage.getParam("serviceSocketIds");
            service.setServiceTemplateId(serviceTemplateId);
            service.setServiceSocketIds(serviceSocketIds);
            isValid(service);
            save(service);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return service;
    }


    @Override
    public void isValid(Service service) throws ParameterValidateException {
        Set<ConstraintViolation<Service>> constraintViolations = validator.validate(service, ServiceChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("service: " + service + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public Service build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<Service> buildAll(Map<String, String> keyValue) {
        if (keyValue.get("name") != null) {
            return repository.findByName(keyValue.get("name"));
        } else if (keyValue.get("serverId") != null && keyValue.get("service-type") != null) {
            List<Service> services = new ArrayList<>();

            Server server = governorOfServer.build(keyValue.get("serverId"));
            if (server != null) {
                for (Service service : server.getServices()) {
                    String serviceType = service.getServiceTemplate().getServiceType().getName();
                    String[] parts = serviceType.split("_");
                    if (keyValue.get("service-type").toUpperCase().equals(serviceType)
                            || keyValue.get("service-type").toUpperCase().equals(parts[0])) {
                        services.add(service);
                    }
                }
            }

            return services;
        } else if (keyValue.get("serverId") != null) {
            Server server = governorOfServer.build(keyValue.get("serverId"));
            if (server != null) {
                return server.getServices();
            }
        } else {
            return repository.findAll();
        }

        return new ArrayList<>();
    }

    @Override
    public void preDelete(String resourceId) {
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("service-id", resourceId);

        Server server = governorOfServer.build(keyValue);

        if (server != null) {
            throw new ParameterValidateException("Я нашла Server с ID " + server.getId()
                    + ", именуемый " + server.getName() + ", так вот в нём имеется удаляемый Service.");
        }
    }
}
