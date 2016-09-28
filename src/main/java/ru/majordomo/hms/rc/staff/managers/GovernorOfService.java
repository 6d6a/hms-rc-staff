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
            if (serviceTemplateId.equals("") ) {
                throw new ParameterValidateException("Параметр serviceTemplate не может быть пустым");
            }
            ServiceTemplate serviceTemplate = templateRepository.findOne(serviceTemplateId);
            if (serviceTemplate == null) {
                throw new ParameterValidateException("ServiceTemplate с ID:" + serviceTemplateId + " не найден");
            }
            service.setServiceTemplate(serviceTemplate);
            List<String> serviceSocketIdList= cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("serviceSocketList"));
            for (String serviceSocketId: serviceSocketIdList) {
                if (serviceSocketId.equals("")) {
                    continue;
                }
                ServiceSocket socket = socketRepository.findOne(serviceSocketId);
                if (socket == null) {
                    continue;
                }
                service.addServiceSocket(socket);
            }

            if (service.getServiceSocketList().isEmpty()) {
                throw new ParameterValidateException("Для сервиса должен быть указан хотя бы один сокет");
            }
            repository.save(service);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return service;
    }
}
