package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;

@Component
public class GovernorOfService extends LordOfResources{

    private ServiceRepository repository;
    private ServiceSocketRepository socketRepository;
    private ServiceTemplateRepository templateRepository;
    private ConfigTemplateRepository configTemplateRepository;
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private GovernorOfServiceSocket governorOfServiceSocket;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(ServiceRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setSocketRepository(ServiceSocketRepository socketRepository) {
        this.socketRepository = socketRepository;
    }

    @Autowired
    public void setTemplateRepository(ServiceTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Autowired
    public void setConfigTemplateRepository(ConfigTemplateRepository configTemplateRepository) {
        this.configTemplateRepository = configTemplateRepository;
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
            service.setServiceTemplate(serviceTemplate);
            service.setServiceSockets(serviceSockets);
            isValid(service);
            repository.save(service);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return service;
    }



    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        Service service = (Service) resource;
        ServiceTemplate serviceTemplate = service.getServiceTemplate();
        if (serviceTemplate == null) {
            throw new ParameterValidateException("Параметр serviceTemplate не может быть null");
        }
        if (serviceTemplate.getId().equals("")) {
            throw new ParameterValidateException("Параметр serviceTemplate не может быть пустым");
        }

        ServiceTemplate storedServiceTemplate = templateRepository.findOne(service.getServiceTemplateId());
        if (storedServiceTemplate == null) {
            throw new ParameterValidateException("ServiceTemplate с ID:" + service.getServiceTemplateId() + " не найден");
        }

        if (serviceTemplate.getConfigTemplates() == null) {
            throw new ParameterValidateException("Параметр ConfigTemaplates не может быть пустым");
        }
        for (ConfigTemplate configTemaplte : serviceTemplate.getConfigTemplates()) {
            if (configTemplateRepository.findOne(configTemaplte.getId()) == null) {
                throw new ParameterValidateException("ConfigTemaplte с ID:" + configTemaplte.getId() + " не найден");
            }
        }

        List<String> serviceSocketIdList= service.getServiceSocketIds();
        if (serviceSocketIdList.isEmpty()) {
            throw new ParameterValidateException("SocketList не может быть пустым");
        }
        for (String serviceSocketId: serviceSocketIdList) {
            if (serviceSocketId.equals("")) {
                throw new ParameterValidateException("ServiceSocketId не может быть пустым");
            }
            ServiceSocket serviceSocket = socketRepository.findOne(serviceSocketId);
            if (serviceSocket == null) {
                throw new ParameterValidateException("ServiceSocket с ID:" + serviceSocketId + " не найден");
            }
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
}
