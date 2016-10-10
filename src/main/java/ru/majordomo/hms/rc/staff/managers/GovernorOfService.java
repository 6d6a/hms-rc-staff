package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfService extends LordOfResources{

    ServiceRepository repository;
    ServiceSocketRepository socketRepository;
    ServiceTemplateRepository templateRepository;
    Cleaner cleaner;

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
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Service service = new Service();
        try {
            LordOfResources.setResourceParams(service, serviceMessage, cleaner);
            String serviceTemplateId = cleaner.cleanString((String) serviceMessage.getParam("serviceTemplate"));
            setServiceTemplateById(service, serviceTemplateId);
            List<String> serviceSocketIds= cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("serviceSocketList"));
            setServiceSocketsByIds(service, serviceSocketIds);

            isValid(service);
            repository.save(service);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return service;
    }

    public void setServiceTemplateById(Service service, String serviceTemplateId) {
        service.setServiceTemplate(templateRepository.findOne(serviceTemplateId));
    }

    public void setServiceSocketsByIds(Service service, List<String> serviceSocketIds) {
        service.setServiceSockets((List<ServiceSocket>)socketRepository.findAll(serviceSocketIds));
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

        List<String> serviceSocketIdList= service.getServiceSocketIds();
        if (serviceSocketIdList.isEmpty()) {
            throw new ParameterValidateException("SocketList не может быть пустым");
        }
        for (String serviceSocketId: serviceSocketIdList) {
            if (serviceSocketId.equals("")) {
                throw new ParameterValidateException("ServiceSocketId не может быть пустым");
            }
            ServiceSocket socket = socketRepository.findOne(serviceSocketId);
            if (socket == null) {
                throw new ParameterValidateException("ServiceSocket с ID:" + serviceSocketId + " не найден");
            }
        }
    }
}
