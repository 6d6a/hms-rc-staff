package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceChecks;

@Component
public class GovernorOfService extends LordOfResources<Service> {

    private ServiceRepository repository;
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
    public Service build(String resourceId) throws ResourceNotFoundException {
        Service service = repository.findOne(resourceId);
        if (service == null) {
            throw new ResourceNotFoundException("Service с ID:" + resourceId + " не найден");
        }

        return service;
    }

    @Override
    public Service build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<Service> buildAll(Map<String, String> keyValue) {

        List<Service> buildedServices = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (Service service : repository.findByName(keyValue.get("name"))) {
                buildedServices.add(build(service.getId()));
            }
        } else {
            for (Service service : repository.findAll()) {
                buildedServices.add(build(service.getId()));
            }
        }

        return buildedServices;
    }

    @Override
    public List<Service> buildAll() {
        List<Service> buildedServices = new ArrayList<>();
        for (Service service : repository.findAll()) {
            buildedServices.add(build(service.getId()));
        }
        return buildedServices;
    }

    @Override
    public void save(Service resource) {
        repository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<Server> servers = governorOfServer.buildAll();
        for (Server server : servers) {
            if (server.getServiceIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла Server с ID " + server.getId()
                        + ", именуемый " + server.getName() + ", так вот в нём имеется удаляемый Service.");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        repository.delete(resourceId);
    }
}
