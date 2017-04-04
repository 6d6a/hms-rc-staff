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
public class GovernorOfService extends LordOfResources<Service> {

    private ServiceRepository repository;
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private GovernorOfServiceSocket governorOfServiceSocket;
    private GovernorOfServer governorOfServer;
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
    public void setGovernorOfServer(GovernorOfServer governorOfServer) {
        this.governorOfServer = governorOfServer;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Service createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Service service = new Service();
        try {
            LordOfResources.setResourceParams(service, serviceMessage, cleaner);
            ServiceTemplate serviceTemplate = (ServiceTemplate) serviceMessage.getParam("serviceTemplate");
            @SuppressWarnings("unchecked") List<ServiceSocket> serviceSockets = (List<ServiceSocket>) serviceMessage.getParam("serviceSockets");
            service.setServiceTemplate(serviceTemplate);
            service.setServiceSockets(serviceSockets);
            isValid(service);
            save(service);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return service;
    }


    @Override
    public void isValid(Service resource) throws ParameterValidateException {
        if (resource.getServiceTemplate() == null || resource.getServiceTemplate().getId() == null || resource.getServiceTemplateId() == null) {
            throw new ParameterValidateException("Отсутствует ServiceTemplate");
        }
        if (resource.getServiceSockets().isEmpty() || resource.getServiceSocketIds().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ServiceSocket");
        }

        //Валидация ServiceTemplate
        ServiceTemplate serviceTemplateToValidate = resource.getServiceTemplate();
        ServiceTemplate serviceTemplateFromRepository = governorOfServiceTemplate.build(serviceTemplateToValidate.getId());
        if (serviceTemplateFromRepository == null) {
            throw new ParameterValidateException("ServiceTemplate с ID: " + serviceTemplateToValidate.getId() + " не найден");
        }
        if (!serviceTemplateFromRepository.equals(serviceTemplateToValidate)) {
            throw new ParameterValidateException("ServiceTemplate с ID: " + serviceTemplateToValidate.getId() + " задан некорректно");
        }

        //Валидация ServiceSockets
        for (ServiceSocket serviceSocketToValidate : resource.getServiceSockets()) {
            ServiceSocket serviceSocketFromRepository = governorOfServiceSocket.build(serviceSocketToValidate.getId());
            if (serviceSocketFromRepository == null) {
                throw new ParameterValidateException("ServiceSocket с ID: " + serviceSocketToValidate.getId() + " не найден");
            }
            if (!serviceSocketFromRepository.equals(serviceSocketToValidate)) {
                throw new ParameterValidateException("ServiceSocket с ID: " + serviceSocketToValidate.getId() + " задан некорректно");
            }
        }

    }

    @Override
    public Service build(String resourceId) throws ResourceNotFoundException {
        Service service = repository.findOne(resourceId);
        if (service == null) {
            throw new ResourceNotFoundException("Service с ID:" + resourceId + " не найден");
        }

        for (String serviceSocketId : service.getServiceSocketIds()) {
            ServiceSocket serviceSocket = governorOfServiceSocket.build(serviceSocketId);
            service.addServiceSocket(serviceSocket);
        }

        ServiceTemplate serviceTemplate = governorOfServiceTemplate.build(service.getServiceTemplateId());
        service.setServiceTemplate(serviceTemplate);

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
