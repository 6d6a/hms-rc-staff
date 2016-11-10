package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;

@Component
public class GovernorOfService extends LordOfResources{

    private ServiceRepository repository;
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private GovernorOfServiceSocket governorOfServiceSocket;
    private GovernorOfServiceType governorOfServiceType;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(ServiceRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServiceTemplate(GovernorOfServiceTemplate governorOfServiceTemplate) {
        this.governorOfServiceTemplate = governorOfServiceTemplate;
    }

    @Autowired
    public void setGovernorOfServiceSocket(GovernorOfServiceSocket governorOfServiceSocket) {
        this.governorOfServiceSocket = governorOfServiceSocket;
    }

    @Autowired
    public void setGovernorOfServiceType(GovernorOfServiceType governorOfServiceType) {
        this.governorOfServiceType = governorOfServiceType;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Service service = new Service();
        try {
            LordOfResources.setResourceParams(service, serviceMessage, cleaner);
            ServiceTemplate serviceTemplate = (ServiceTemplate) serviceMessage.getParam("serviceTemplate");
            List<ServiceSocket> serviceSockets = (List<ServiceSocket>) serviceMessage.getParam("serviceSockets");
            ServiceType serviceType = (ServiceType) serviceMessage.getParam("serviceType");
            service.setServiceTemplate(serviceTemplate);
            service.setServiceSockets(serviceSockets);
            service.setServiceType(serviceType);
            isValid(service);
            save(service);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return service;
    }



    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        Service service = (Service) resource;

        if (service.getServiceTemplate() == null || service.getServiceTemplate().getId() == null || service.getServiceTemplateId() == null) {
            throw new ParameterValidateException("Отсутствует ServiceTemplate");
        }
        if (service.getServiceSockets().isEmpty() || service.getServiceSocketIds().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ServiceSocket");
        }
        if (service.getServiceType() == null) {
            throw new ParameterValidateException("Отсутствует ServiceType");
        }

        //Валидация ServiceTemplate
        ServiceTemplate serviceTemplateToValidate = service.getServiceTemplate();
        ServiceTemplate serviceTemplateFromRepository = (ServiceTemplate) governorOfServiceTemplate.build(serviceTemplateToValidate.getId());
        if (serviceTemplateFromRepository == null) {
            throw new ParameterValidateException("ServiceTemplate с ID: " + serviceTemplateToValidate.getId() + " не найден");
        }
        if(!serviceTemplateFromRepository.equals(serviceTemplateToValidate)) {
            throw new ParameterValidateException("ServiceTemplate с ID: " + serviceTemplateToValidate.getId() + " задан некорректно");
        }

        //Валидация ServiceSockets
        for (ServiceSocket serviceSocketToValidate : service.getServiceSockets()) {
            ServiceSocket serviceSocketFromRepository = (ServiceSocket) governorOfServiceSocket.build(serviceSocketToValidate.getId());
            if (serviceSocketFromRepository == null) {
                throw new ParameterValidateException("ServiceSocket с ID: " + serviceSocketToValidate.getId() + " не найден");
            }
            if(!serviceSocketFromRepository.equals(serviceSocketToValidate)) {
                throw new ParameterValidateException("ServiceSocket с ID: " + serviceSocketToValidate.getId() + " задан некорректно");
            }
        }

        //Валидация ServiceType
        try {
            governorOfServiceType.build(service.getServiceType().getName());
        } catch (ResourceNotFoundException e)  {
            throw new ParameterValidateException("ServiceType с именем: " + service.getServiceType().getName() + " не найден");
        }

    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        Service service = repository.findOne(resourceId);
        if (service == null) {
            throw new ResourceNotFoundException("Service с ID:" + resourceId + " не найден");
        }

        for (String serviceSocketId: service.getServiceSocketIds()) {
            ServiceSocket serviceSocket = (ServiceSocket) governorOfServiceSocket.build(serviceSocketId);
            service.addServiceSocket(serviceSocket);
        }

        ServiceTemplate serviceTemplate = (ServiceTemplate) governorOfServiceTemplate.build(service.getServiceTemplateId());
        service.setServiceTemplate(serviceTemplate);

        return service;
    }

    @Override
    public Resource build(Map<String, String> keyValue) throws NotImplementedException {
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
                buildedServices.add((Service) build(service.getId()));
            }
        } else {
            for (Service service : repository.findAll()) {
                buildedServices.add((Service) build(service.getId()));
            }
        }

        return buildedServices;
    }

    @Override
    public List<Service> buildAll() {
        List<Service> buildedServices = new ArrayList<>();
        for (Service service : repository.findAll()) {
            buildedServices.add((Service) build(service.getId()));
        }
        return buildedServices;
    }

    @Override
    public void save(Resource resource) {
        repository.save((Service) resource);
    }

    @Override
    public void delete(String resourceId) {
        repository.delete(resourceId);
    }

}
